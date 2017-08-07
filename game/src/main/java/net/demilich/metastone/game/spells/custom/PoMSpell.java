package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class PoMSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = context.getEventCard();
		CardCollection removes = new CardCollection();
		CardCollection adds = new CardCollection();
		for (Card deckCard : player.getDeck()) {
			if (deckCard.getCardId() == card.getCardId()) {
				removes.add(deckCard);
				adds.add(deckCard);
			}
		}
		for (Card remove : removes) {
			context.getLogic().removeCardFromDeck(player.getId(), remove);
		}
		for (Card add : adds) {
			context.getLogic().receiveCard(player.getId(), add);
		}
		
	}

}
