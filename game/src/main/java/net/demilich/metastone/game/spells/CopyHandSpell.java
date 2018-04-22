package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.CardLocation;

import java.util.List;

public class CopyHandSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        List<Entity> targetHand = context.resolveTarget(player, source,desc.getTarget());
        context.getLogic().removeAllCards(player.getId());
        for (Entity entity : targetHand) {
            Card card = (Card) entity;
            Card newCard = context.getCardById(card.getCardId());
            for (Attribute attribute : card.getAttributes().keySet()) {
                newCard.setAttribute(attribute, card.getAttribute(attribute));
            }
            context.getLogic().receiveCard(player.getId(), newCard);
        }
    }
}
