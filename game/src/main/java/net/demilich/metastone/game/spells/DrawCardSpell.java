package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.CardLocation;

public class DrawCardSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target != null && target instanceof Card) {
			Card card = (Card) target;
			if (card.getLocation().equals(CardLocation.DECK) && player.getDeck().contains(card)) {
				context.getLogic().drawCard(player.getId(), card, source);
			}
		} else {
			int cardCount = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
			for (int i = 0; i < cardCount; i++) {
				context.getLogic().drawCard(player.getId(), source);
			}
		}

	}
}
