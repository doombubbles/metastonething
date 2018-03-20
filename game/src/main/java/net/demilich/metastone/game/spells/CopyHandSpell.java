package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

public class CopyHandSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        List<Entity> targetHand = context.resolveTarget(player, source,desc.getTarget());
        CardList hand = player.getHand();
        for (Card card : hand) {
            context.getLogic().removeCardFromHand(player.getId(), card);
        }
        for (Entity entity : targetHand) {
            Card card = (Card) entity;
            context.getLogic().receiveCard(player.getId(), card.clone());
        }
    }
}
