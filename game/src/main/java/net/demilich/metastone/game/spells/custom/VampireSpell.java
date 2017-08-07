package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class VampireSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);
		Card card = opponent.getDeck().get(0);
		context.fireGameEvent(new CardRevealedEvent(context, player.getId(), card, 1.5));
		context.fireGameEvent(new CardRevealedEvent(context, opponent.getId(), card, 1.5));
		opponent.getDeck().removeFirst();
	}

}
