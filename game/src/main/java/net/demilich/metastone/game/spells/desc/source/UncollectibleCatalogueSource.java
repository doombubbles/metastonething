package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardSet;

public class UncollectibleCatalogueSource extends CardSource {

    public UncollectibleCatalogueSource(SourceDesc desc) {
        super(desc);
    }

    @Override
    protected CardList match(GameContext context, Player player) {
        return CardCatalogue.getAll(context.getDeckFormat());
    }
}
