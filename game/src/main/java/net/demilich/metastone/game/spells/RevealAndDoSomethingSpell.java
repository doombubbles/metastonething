package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;

public class RevealAndDoSomethingSpell extends Spell {

    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardFilter cardFilter = (CardFilter) desc.get(SpellArg.CARD_FILTER);
        CardList deck = player.getDeck().clone();
        deck.shuffle();
        Card chosenCard = null;
        for (Card card : deck) {
            if (cardFilter.matches(context, player, card)) {
                chosenCard = card;
                break;
            }
        }
        if (chosenCard == null) {
            return;
        }
        int howMany = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
        for (int i = 1; i <= howMany; i++) {
            context.fireGameEvent(new CardRevealedEvent(context, player.getId(), chosenCard, 1.2 * i));
            SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
            context.setEventCard(chosenCard);
            SpellUtils.castChildSpell(context, player, spell, source, target);
        }

    }
}
