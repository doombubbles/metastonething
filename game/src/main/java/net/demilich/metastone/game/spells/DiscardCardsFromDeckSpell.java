package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class DiscardCardsFromDeckSpell extends Spell {

	public static SpellDesc create(int howMany) {
		Map<SpellArg, Object> arguments = SpellDesc.build(DiscardCardsFromDeckSpell.class);
		arguments.put(SpellArg.VALUE, howMany);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int howMany = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		for (int i = 0; i < howMany; i++) {
			// Question: If I have no cards left and my Fel Reaver discards 3,
			// do I draw 3 Fatigues or do I only Fatigue more when I draw a
			// card?
			// Answer: Fel Reaver won't trigger fatigue
			// Source: Blue post
			if (player.getDeck().isEmpty()) {
				return;
			}
			Card card = player.getDeck().get(0);
			context.fireGameEvent(new CardRevealedEvent(context, player.getId(), card, 1.2 + 1.2 * i));
			context.fireGameEvent(new CardRevealedEvent(context, context.getOpponent(player).getId(), card, 1.2 + 1.2 * i));
			context.getLogic().removeCardFromDeck(player.getId(), card);
		}
	}

}
