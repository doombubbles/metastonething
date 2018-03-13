package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;

public class CastSpellFromSourceSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardFilter filter = (CardFilter) desc.get(SpellArg.CARD_FILTER);
        CardList spells = CardCatalogue.query(context.getDeckFormat(), CardType.SPELL);
        CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
        Boolean useUp = desc.getBool(SpellArg.EXCLUSIVE);
        if (cardSource != null) {
            spells = cardSource.getCards(context, player);
        }
        CardList filteredSpells = new CardList();
        for (Card spell : spells) {
            if ((filter == null || filter.matches(context, player, spell)) && spell.getCardType().equals(CardType.SPELL)) {
                SpellCard spellCard = (SpellCard) spell;
                if (spellCard.canBeCast(context, player)) {
                    filteredSpells.add(spell);
                }
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
