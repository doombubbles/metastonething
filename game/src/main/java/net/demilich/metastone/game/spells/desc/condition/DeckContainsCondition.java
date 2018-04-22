package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

public class DeckContainsCondition extends Condition {

	public DeckContainsCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(ConditionArg.CARD_FILTER);
		String[] cardIds = (String[]) desc.get(ConditionArg.CARD_IDS);
		if (cardFilter != null) {
			for (Card card : player.getDeck()) {
				if (cardFilter == null || cardFilter.matches(context, player, card)) {
					return true;
				}
			}
		} else {
			for (String s : cardIds) {
				if (player.getDeck().contains(context.getCardById(s))) {
					return true;
				}
			}
		}

		return false;
	}

}
