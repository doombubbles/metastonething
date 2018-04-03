package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

public class CardPropertyCondition extends Condition {

	public CardPropertyCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		Card card = null;
		if (desc.contains(ConditionArg.TARGET)) {
			List<Entity> targets = context.resolveTarget(player, source, (EntityReference) desc.get(ConditionArg.TARGET));
			if (targets != null) {
				target = targets.get(0);
			}
		}


		//System.out.println(target.getName() + " is a " + target.getEntityType());
		if (target instanceof Card) {
			card = (Card) target;
		} else if (target instanceof Actor) {
			Actor actor = (Actor) target;
			card = actor.getSourceCard();
		}
		if (card == null) {
			return false;
		}
		CardType cardType = (CardType) desc.get(ConditionArg.CARD_TYPE);
		if (cardType != null && !card.getCardType().isCardType(cardType)) {
			return false;
		}

		EntityFilter filter = (EntityFilter) desc.get(ConditionArg.FILTER);
		if (filter != null) {
			if (!filter.matches(context, player, card)) {
				return false;
			}
		}

		String cardId = (String) desc.get(ConditionArg.CARD_ID);
		if (cardId != null && !card.getCardId().contains(cardId)) {
			return false;
		}

		return true;
	}

}
