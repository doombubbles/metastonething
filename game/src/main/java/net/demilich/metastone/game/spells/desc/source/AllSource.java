package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;

public class AllSource extends CardSource {

    public AllSource(SourceDesc desc) {
        super(desc);
    }

    @Override
    protected CardList match(GameContext context, Player player) {
        return CardCatalogue.getAll();
    }
}
