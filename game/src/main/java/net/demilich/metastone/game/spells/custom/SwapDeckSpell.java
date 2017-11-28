package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SwapDeckSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardCollection deck1 = context.getPlayer1().getDeck();
        CardCollection deck2 = context.getPlayer2().getDeck();
        for (Card card : context.getPlayer1().getDeck()) {
            context.getLogic().removeCardFromDeck(context.getPlayer1().getId(), card);
        }
        for (Card card : context.getPlayer2().getDeck()) {
            context.getLogic().removeCardFromDeck(context.getPlayer2().getId(), card);
        }
        for (Card card : deck1) {
            context.getLogic().shuffleToDeck(context.getPlayer2(), card);
        }
        for (Card card : deck2) {
            context.getLogic().shuffleToDeck(context.getPlayer1(), card);
        }
    }
}
