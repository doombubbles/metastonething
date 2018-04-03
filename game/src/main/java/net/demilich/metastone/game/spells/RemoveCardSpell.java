package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.CardLocation;

public class RemoveCardSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = (Card) target;
		if (card == null) {
			return;
		}
		if (desc.contains(SpellArg.REVEAL)) {
			context.fireGameEvent(new CardRevealedEvent(context, player.getId(), card,1.2));
		}
		if (card.getCardReference().getLocation().equals(CardLocation.DECK)) {
			context.getLogic().removeCardFromDeck(player.getId(), card);
		} else context.getLogic().removeCardFromHand(player.getId(), card);
		
	}

}
