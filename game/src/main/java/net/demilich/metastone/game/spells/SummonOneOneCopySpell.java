package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;

public class SummonOneOneCopySpell extends Spell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonOneOneCopySpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		CardList relevantMinions = null;
		if (cardSource != null) {
			CardList allCards = cardSource.getCards(context, player);
			allCards.shuffle();
			relevantMinions = new CardList();
			for (Card card : allCards) {
				if (card.getCardType().isCardType(CardType.MINION) && (cardFilter == null || cardFilter.matches(context, player, card))) {
					relevantMinions.add(card);
				}
			}
		}
		int count = desc.getInt(SpellArg.VALUE, 1);
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		MinionCard minionCard = null;
		for (int i = 0; i < count; i++) {
			if (cardSource != null || cardFilter != null) {
				if (cardSource == null) {
					CardList allMinions = CardCatalogue.query(context.getDeckFormat(), CardType.MINION);
					relevantMinions = new CardList();
					for (Card card : allMinions) {
						if (cardFilter.matches(context, player, card)) {
							relevantMinions.add(card);
						}
					}
				}
				minionCard = (MinionCard) relevantMinions.getRandom();
			} else {
				if (target instanceof Minion) {
					minionCard = (MinionCard) ((Minion) target).getSourceCard();
				} else if (target instanceof Card) {
					minionCard = (MinionCard) target;
				}
			}

			if (minionCard != null) {
				Minion minion = minionCard.summon();
				if (desc.contains(SpellArg.EXCLUSIVE)) {
					relevantMinions.remove(minionCard);
				}
				if (context.getLogic().summon(player.getId(), minion, null, boardPosition, false)) {
					minion.setAttack(desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, null, 1));
					minion.setHp(desc.getValue(SpellArg.HP_BONUS, context, player, target, null, 1));
					minion.setMaxHp(desc.getValue(SpellArg.HP_BONUS, context, player, target, null, 1));
				}
			}
		}
	}

}