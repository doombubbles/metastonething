package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SwapHandWithDeckSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardCollection deck = player.getDeck().clone();
        CardCollection hand = player.getHand().clone();

        player.getDeck().removeAll();
        player.getHand().removeAll();

        int i = 0;
        while (i < 10) {
            if (deck.getCount() > i) {
                context.getLogic().receiveCard(player.getId(), deck.get(i));
            }
            i++;
        }
        player.getDeck().addAll(hand);
    }
}
