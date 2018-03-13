package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;

public class SupremeArchiveSource extends CardSource {
    public SupremeArchiveSource(SourceDesc desc) {
        super(desc);
    }

    @Override
    protected CardList match(GameContext context, Player player) {
        return player.getSupremeArchive().clone();
    }
}
