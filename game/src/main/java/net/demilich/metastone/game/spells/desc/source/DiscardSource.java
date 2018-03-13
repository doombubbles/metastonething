package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;

public class DiscardSource extends CardSource {

	public DiscardSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Player player) {
		CardList discardedCards = new CardList();
		player.getDiscarded().forEach(card -> discardedCards.add(card));
		return discardedCards;
	}

}
