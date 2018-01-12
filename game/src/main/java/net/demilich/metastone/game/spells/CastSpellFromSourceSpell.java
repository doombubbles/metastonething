package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.DeckSource;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.spells.desc.source.SourceArg;

public class CastSpellFromSourceSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardFilter filter = (CardFilter) desc.get(SpellArg.CARD_FILTER);
        CardCollection spells = CardCatalogue.query(context.getDeckFormat(), CardType.SPELL);
        CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
        Boolean useUp = desc.getBool(SpellArg.EXCLUSIVE);
        if (cardSource != null) {
            spells = cardSource.getCards(context, player);
        }
        CardCollection filteredSpells = new CardCollection();
        for (Card spell : spells) {
            if ((filter == null || filter.matches(context, player, spell)) && spell.getCardType().equals(CardType.SPELL) && context.getLogic().canPlayCard(player.getId(), spell.getCardReference())) {
                filteredSpells.add(spell);
            }
        }

        int numberOfSpellsToCast = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
        for (int i = 0; i < numberOfSpellsToCast; i++) {
            Card randomCard = filteredSpells.getRandom();
            if (randomCard == null) {
                return;
            }
            if (useUp) {
                for (Card card : player.getDeck()) {
                    if (randomCard.getCardId() == card.getCardId()) {
                        randomCard = card;
                    }
                }
                player.getDeck().remove(randomCard);
            }
            SpellDesc cast = CastRandomSpellSpell.create(randomCard.getCardId());
            SpellUtils.castChildSpell(context, player, cast, source, target);
        }
    }
}
