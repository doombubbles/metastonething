package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

import java.util.Arrays;
import java.util.List;

public class SpecificCardFilter extends EntityFilter {
	
	public SpecificCardFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean test(GameContext context, Player player, Entity entity, Entity source) {
		String cardId = null;
		if (entity instanceof Card) {
			cardId = ((Card) entity).getCardId();
		} else if (entity instanceof Actor) {
			cardId = ((Actor) entity).getSourceCard().getCardId();
		}
		String requiredCardId = desc.getString(FilterArg.CARD_ID);
		List<String> cardIds = null;
		if (desc.contains(FilterArg.CARDS)) {
			cardIds = Arrays.asList((String[]) desc.get(FilterArg.CARDS));
			return cardIds.contains(cardId);
		} else {
			if (requiredCardId.equalsIgnoreCase("EVENT_CARD")) {
				requiredCardId = context.getEventCard().getCardId();
			}
			if (context.getLogic().hasAttribute(player, Attribute.MINIONS_COUNT_AS)) {
				for (Object o : context.getLogic().getAttributes(player, Attribute.MINIONS_COUNT_AS)) {
					if (((List<String>) o).contains(requiredCardId)) {
						return true;
					}
				}
			}
			return cardId.equalsIgnoreCase(requiredCardId);
		}

	}

}
