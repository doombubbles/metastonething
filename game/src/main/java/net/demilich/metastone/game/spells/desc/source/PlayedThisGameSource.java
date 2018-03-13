package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;

public class PlayedThisGameSource extends CardSource {

	public PlayedThisGameSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Player player) {
		CardList minionCards = new CardList();
		player.minionsPlayed.forEach(minionCard -> minionCards.add(minionCard));
		return minionCards;
	}

}
