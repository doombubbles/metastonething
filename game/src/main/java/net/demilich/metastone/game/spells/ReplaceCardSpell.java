package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.CardLocation;

public class ReplaceCardSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter filter1 = (EntityFilter) desc.get(SpellArg.FILTER);
		EntityFilter filter2 = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		String cardId = desc.getString(SpellArg.CARD);
		Card cardTarget = (Card) target;
		CardList result = new CardList();
		if (filter2 != null) {
			CardList cards = CardCatalogue.query(context.getDeckFormat());
			for (Card card : cards) {
				if (filter2.matches(context, player, card)) {
					result.add(card);
				}
			}
		} else result.add(context.getCardById(cardId));

		if (cardTarget.getLocation() == CardLocation.HAND && filter1.matches(context, player, cardTarget)) {
			context.getLogic().replaceCard(player.getId(), cardTarget, result.getRandom());
		}
		if (cardTarget.getLocation() == CardLocation.DECK && filter1.matches(context, player, cardTarget)) {
			context.getLogic().replaceCardInDeck(player.getId(), cardTarget, result.getRandom());
		}
	} 

}
