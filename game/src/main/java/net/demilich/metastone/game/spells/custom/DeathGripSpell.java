package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class DeathGripSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);
		CardCollection minions = new CardCollection();
		for (Card card : opponent.getDeck()) {
			if (card.getCardType().equals(CardType.MINION)) {
				minions.add(card);
			}
		}
		Card minion = minions.getRandom();
		opponent.getDeck().remove(minion);
		context.getLogic().receiveCard(player.getId(), context.getCardById(minion.getCardId()));
		
	}

}
