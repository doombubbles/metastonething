package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class LilianSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = new CardList();
		CardList cards2 = CardCatalogue.query(context.getDeckFormat());
		cards2.removeAll(p -> p.getHeroClass() != context.getOpponent(player).getHero().getHeroClass());
		for (Card card : player.getHand()) {
			if (card.getCardType() == CardType.SPELL) {
				cards.add(card);
			}
		}
		for (Card card : cards) {
			context.getLogic().replaceCard(player.getId(), card, cards2.getRandom());
		}
		
	}

}
