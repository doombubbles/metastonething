package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;

public class DiscoverDrawSpell extends Spell {
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = new CardList();
		CardFilter filter = (CardFilter) desc.getEntityFilter();
		if (player.getDeck().isEmpty()) {
			return;
		}

		CardList deckCards = new CardList();
		for (Card card : player.getDeck()) {
			if (filter != null) {
				if (filter.matches(context, player, card)) {
					deckCards.add(card);
				}
			} else deckCards.add(card);
		}

		boolean discard = desc.getBool(SpellArg.EXCLUSIVE);
		
		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		for (int i = 0; i < count; i++) {
			if (!player.getDeck().isEmpty()) {
				Card card = deckCards.peekFirst();
				deckCards.remove(card);
				if (discard) {
					context.getLogic().removeCardFromDeck(player.getId(), card);
				}
				cards.add(card);

				if (context.getLogic().hasAttribute(player, Attribute.ALL_OPTIONS)) {
					SpellUtils.castChildSpell(context, player, ((SpellDesc) desc.get(SpellArg.SPELL)).addArg(SpellArg.CARD, card.getCardId()), source, target);
					if (!discard) {
						context.getLogic().removeCardFromDeck(player.getId(), card);
					}
				}
			}
		}

		if (cards.isEmpty() || context.getLogic().hasAttribute(player, Attribute.ALL_OPTIONS)) {
			return;
		}

		DiscoverAction action = SpellUtils.getDiscover(context, player, desc, cards);
		SpellUtils.castChildSpell(context, player, action.getSpell(), source, target);
		if (!discard) {
			context.getLogic().removeCardFromDeck(player.getId(), action.getCard());
		}
	}

}
