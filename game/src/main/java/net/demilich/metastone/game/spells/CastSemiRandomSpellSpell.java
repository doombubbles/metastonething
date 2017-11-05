package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
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
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class CastSemiRandomSpellSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardFilter filter = (CardFilter) desc.get(SpellArg.CARD_FILTER);
		CardCollection spells = CardCatalogue.query(context.getDeckFormat(), CardType.SPELL);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		EntityType targetType = target.getEntityType();
		List<TargetSelection> okTargets = new ArrayList<TargetSelection>();
		switch (targetType) {
			case MINION:
				okTargets.add(TargetSelection.ANY);
				okTargets.add(TargetSelection.MINIONS);
				if (target.getOwner() == player.getId()) {
					okTargets.add(TargetSelection.FRIENDLY_CHARACTERS);
					okTargets.add(TargetSelection.FRIENDLY_MINIONS);
				} else {
					okTargets.add(TargetSelection.ENEMY_MINIONS);
					okTargets.add(TargetSelection.ENEMY_CHARACTERS);
				}
				break;
		}
		
		if (cardSource != null) {
			spells = cardSource.getCards(context, player);
		}
		CardCollection filteredSpells = new CardCollection();
		for (Card spell : spells) {
			if (filter == null || filter.matches(context, player, spell)) {
				if (((SpellCard) spell).getTargetRequirement() != TargetSelection.NONE && okTargets.contains(((SpellCard) spell).getTargetRequirement())) {
					filteredSpells.add(spell);
				}
			}
		}
		
		int numberOfSpellsToCast = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < numberOfSpellsToCast; i++) {
			Card randomCard = filteredSpells.getRandom();
			CardRevealedEvent revealEvent = new CardRevealedEvent(context, player.getId(), randomCard, 1.2 * (i + 1));
			context.fireGameEvent(revealEvent);
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
				battlecry.setSource(copyCard.getReference());
				battlecry.setTarget(target);
				context.getLogic().performGameAction(player.getId(), battlecry);
				
				if (spellCard.hasAttribute(Attribute.OVERLOAD)) {
					player.modifyAttribute(Attribute.OVERLOAD, spellCard.getAttributeValue(Attribute.OVERLOAD));
					context.fireGameEvent(new OverloadEvent(context, player.getId(), spellCard, spellCard.getAttributeValue(Attribute.OVERLOAD)));
				}
			}
			context.getLogic().checkForDeadEntities();
		}

	}

}
