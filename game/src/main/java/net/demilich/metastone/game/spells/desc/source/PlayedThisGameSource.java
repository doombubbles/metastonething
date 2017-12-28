package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCollection;

public class PlayedThisGameSource extends CardSource {

	public PlayedThisGameSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardCollection match(GameContext context, Player player) {
		CardCollection minionCards = new CardCollection();
		player.minionsPlayed.forEach(minionCard -> minionCards.add(minionCard));
		return minionCards;
	}

}
