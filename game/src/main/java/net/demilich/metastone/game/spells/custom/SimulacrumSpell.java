package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SimulacrumSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardCollection cards = new CardCollection();
		CardCollection cards2 = new CardCollection();
		for (Card card : player.getHand()) {
			if (card.getCardType() == CardType.MINION) {
				cards.add(card);
			}
		}
		cards.sortByManaCost();
		for (Card card : cards) {
			if (card.getManaCost(context, player) <= cards.get(0).getManaCost(context, player)) {
				cards2.add(card);
			}
		}
		if (cards2.isEmpty()) {
			return;
		}
		context.getLogic().receiveCard(player.getId(), context.getCardById(cards2.getRandom().getCardId()));
	}

}
