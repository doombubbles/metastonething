package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;

public class WildSource extends CardSource {
    public WildSource(SourceDesc desc) {
        super(desc);
    }

    @Override
    protected CardList match(GameContext context, Player player) {
        DeckFormat wildExclusive = new DeckFormat();
        wildExclusive.addSet(CardSet.HALL_OF_FAME);
        wildExclusive.addSet(CardSet.NAXXRAMAS);
        wildExclusive.addSet(CardSet.GOBLINS_VS_GNOMES);
        wildExclusive.addSet(CardSet.BLACKROCK_MOUNTAIN);
        wildExclusive.addSet(CardSet.THE_GRAND_TOURNAMENT);
        wildExclusive.addSet(CardSet.LEAGUE_OF_EXPLORERS);
        wildExclusive.addSet(CardSet.THE_OLD_GODS);
        wildExclusive.addSet(CardSet.ONE_NIGHT_IN_KARAZHAN);
        wildExclusive.addSet(CardSet.MEAN_STREETS_OF_GADGETZAN);
        return CardCatalogue.query(wildExclusive);
    }
}
