package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class StealBananaSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);
		for (Card card : opponent.getHand()) {
			if (card.getCardId() == CardCatalogue.getCardById("spell_bananas").getCardId()) {
				context.getLogic().discardCard(opponent, card);
				context.getLogic().receiveCard(player.getId(), card);
			}
		}
	}

}
