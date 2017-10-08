package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCollection;

public class DiscardSource extends CardSource {

	public DiscardSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardCollection match(GameContext context, Player player) {
		CardCollection discardedCards = new CardCollection();
		player.getDiscarded().forEach(card -> discardedCards.add(card));
		return discardedCards;
	}

}
