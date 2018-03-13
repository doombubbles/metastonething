package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class RemoveFromDeckSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		
		String cardId = (String) desc.get(SpellArg.CARD);
		Card card = context.getCardById(cardId);
		
		
		for (Card other : player.getDeck()) {
			if (other.getCardId().equals(card.getCardId())) {
				context.getLogic().removeCardFromDeck(player.getId(), other);
				return;
			}
		}
		
		
	}

}
