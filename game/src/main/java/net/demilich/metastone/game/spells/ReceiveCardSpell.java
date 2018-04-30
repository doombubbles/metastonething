package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;

import java.util.Map;

public class ReceiveCardSpell extends Spell {

	public static SpellDesc create(Player player) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveCardSpell.class);
		arguments.put(SpellArg.TARGET_PLAYER, player.getReference());
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		if (cardFilter != null || cardSource != null) {
			CardList cards;
			if (cardSource != null) {
				cards = cardSource.getCards(context, player);
			} else cards = CardCatalogue.query(context.getDeckFormat());
			CardList result = new CardList();
			String replacementCard = (String) desc.get(SpellArg.CARD);
			for (Card card : cards) {
				if (cardFilter != null) {
					if (cardFilter.matches(context, player, card)) {
						result.add(card);
					}
				} else result.add(card);
			}
			for (int i = 0; i < count; i++) {
				Card card = null;
				if (!result.isEmpty()) {
					card = result.getRandom();
				} else if (replacementCard != null) {
					card = context.getCardById(replacementCard);
				}
				if (card != null) {
					Card clone = card.clone();
					if (attribute != null) {
						clone.setAttribute(attribute);
					}
					context.getLogic().receiveCard(player.getId(), clone);
				}
			}
		} else {
			for (Card card : SpellUtils.getCards(context, desc, player)) {
				for (int i = 0; i < count; i++) {
					Card local = card.clone();
					if (attribute != null) {
						local.setAttribute(attribute);
					}
					context.getLogic().receiveCard(player.getId(), local);
				}
			}
		}
		
	}

}
