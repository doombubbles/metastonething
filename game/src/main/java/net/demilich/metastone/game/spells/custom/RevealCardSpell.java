package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class RevealCardSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = desc.get(SpellArg.VALUE) != null ? desc.getInt(SpellArg.VALUE) : 1;
		Card card = context.getCardById(desc.getString(SpellArg.CARD));
		
		for (int i = 0; i < value; i++) {
			context.fireGameEvent(new CardRevealedEvent(context, context.getPlayer1().getId(), card, (1 + i) * 1.2));
			context.fireGameEvent(new CardRevealedEvent(context, context.getPlayer2().getId(), card, (1 + i) * 1.2));
		}
			
		
	}

}
