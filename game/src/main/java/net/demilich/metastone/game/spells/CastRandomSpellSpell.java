package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.ChooseOneCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.events.OverloadEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.targeting.TargetSelection;

public class CastRandomSpellSpell extends Spell {
	
	Logger logger = LoggerFactory.getLogger(CastRandomSpellSpell.class);

	public static SpellDesc create(int value) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CastRandomSpellSpell.class);
		arguments.put(SpellArg.VALUE, value);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(String card) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CastRandomSpellSpell.class);
		arguments.put(SpellArg.CARD, card);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(String card, boolean reveal) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CastRandomSpellSpell.class);
		arguments.put(SpellArg.CARD, card);
		arguments.put(SpellArg.REVEAL, reveal);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// This spell is crazy.
		CardFilter filter = (CardFilter) desc.get(SpellArg.CARD_FILTER);
		CardList spells = CardCatalogue.query(context.getDeckFormat(), CardType.SPELL);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		boolean tryAgain = desc.getBool(SpellArg.EXCLUSIVE);
		boolean reveal = true;
		if (desc.contains(SpellArg.REVEAL)) {
			reveal = desc.getBool(SpellArg.REVEAL);
		}
		if (cardSource != null) {
			spells = cardSource.getCards(context, player);
		}
		CardList filteredSpells = new CardList();
		for (Card spell : spells) {
			if ((filter == null || filter.matches(context, player, spell))) {
				filteredSpells.add(spell);
			}
		}
		// Straight up insane.
		
		// Set behavior to random. Because we're already insane.
		// This allows Discover effects and targeting to actually be random.
		Player originalPlayer = player;
		Player opponent = context.getOpponent(player);
		opponent.setAttribute(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION, true);
		player.setAttribute(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION, true); 
		// HAHAHAHAHAHAHAHAHAHA!
		int numberOfSpellsToCast = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < numberOfSpellsToCast; i++) {
			// In case Yogg changes sides, this should case who the spells are being cast for.
			if (source.getEntityType().equals(EntityType.MINION)) {
				player = context.getPlayer(source.getOwner());
			}
			// If Yogg is removed from the board, stop casting spells.
			if (!player.getSummons().contains(source) && source.getEntityType().equals(EntityType.MINION)) {
				System.out.println("He's dead jim " + source.getEntityType().name());
				break;
			}
			Card randomCard;
			if (desc.contains(SpellArg.CARD)) {
				randomCard = context.getCardById(desc.getString(SpellArg.CARD));
			} else if (filter != null) {
				randomCard = filteredSpells.getRandom();
			} else {
				randomCard = (Card) target;
			}
			if (tryAgain) {
				while (!((SpellCard) randomCard).canBeCast(context, player)) {
					randomCard = filteredSpells.getRandom();
				}
			}
			logger.debug("Yogg-Saron chooses to play " + randomCard.getName());
			if (reveal) {
				CardRevealedEvent revealEvent = new CardRevealedEvent(context, player.getId(), randomCard, 1.2 * (i + 1));
				context.fireGameEvent(revealEvent);
			}
			if (randomCard instanceof ChooseOneCard && !context.getLogic().hasAttribute(player, Attribute.BOTH_CHOOSE_ONE_OPTIONS)) {
				// While it might seem odd to do this, Choose One spells are still chosen
				// randomly, even if the choice isn't available.
				ChooseOneCard chooseOneCard = (ChooseOneCard) randomCard;
				Card[] cards = chooseOneCard.getChoiceCards();
				randomCard = cards[context.getLogic().random(cards.length)];
			} else if (randomCard instanceof ChooseOneCard && context.getLogic().hasAttribute(player, Attribute.BOTH_CHOOSE_ONE_OPTIONS)) {
				ChooseOneCard chooseOneCard = (ChooseOneCard) randomCard;
				randomCard = chooseOneCard.getBothChoicesCard();
			}
			SpellCard spellCard = (SpellCard) randomCard;
			if (spellCard.canBeCast(context, player)) {
				SpellCard copyCard = spellCard.clone();
				context.getLogic().drawSetAsideCard(player.getId(), copyCard);

				BattlecryAction battlecry = BattlecryAction.createBattlecry(copyCard.getSpell(), copyCard.getTargetRequirement());
				GameAction battlecryAction = null;
				battlecry.setSource(copyCard.getReference());
				if (battlecry.getTargetRequirement() != TargetSelection.NONE) {
					List<Entity> validTargets = context.getLogic().getValidTargets(player.getId(), battlecry);
					if (validTargets.isEmpty()) {
						continue;
					}

					List<GameAction> battlecryActions = new ArrayList<>();
					for (Entity validTarget : validTargets) {
						GameAction targetedBattlecry = battlecry.clone();
						targetedBattlecry.setTarget(validTarget);
						battlecryActions.add(targetedBattlecry);
					}
					battlecryAction = battlecryActions.get(context.getLogic().random(battlecryActions.size()));
				} else {
					battlecryAction = battlecry;
				}
				context.getLogic().performGameAction(player.getId(), battlecryAction);
				// If the card has Overload, then start overloading...
				if (spellCard.hasAttribute(Attribute.OVERLOAD)) {
					player.modifyAttribute(Attribute.OVERLOAD, spellCard.getAttributeValue(Attribute.OVERLOAD));
					context.fireGameEvent(new OverloadEvent(context, player.getId(), spellCard, spellCard.getAttributeValue(Attribute.OVERLOAD)));
				}
			}
			// Technically, this is only half correct. Yogg-Saron should not stop if
			// your opponent has died, but should if you do. But, it works for now.
			context.getLogic().checkForDeadEntities();
		}
		opponent.removeAttribute(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION);
		originalPlayer.removeAttribute(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION);
		// *ahem* Back to normal.
	}

}