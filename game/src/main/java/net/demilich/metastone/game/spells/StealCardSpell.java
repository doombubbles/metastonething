package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.targeting.CardLocation;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class StealCardSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        if (target == null || !target.getEntityType().equals(EntityType.CARD)) {
            return;
        }
        Card targetCard = (Card) target;
        CardLocation cardLocation = (CardLocation) desc.get(SpellArg.CARD_LOCATION);
        Card newCard = context.getCardById(targetCard.getCardId());
        for (Attribute attribute : targetCard.getAttributes().keySet()) {
            newCard.setAttribute(attribute, targetCard.getAttribute(attribute));
        }
        for (IGameEventListener iGameEventListener : context.getTriggersAssociatedWith(targetCard.getReference())) {
            context.getLogic().addGameEventListener(player, iGameEventListener, newCard);
        }
        if (cardLocation.equals(CardLocation.HAND)) {
            context.getLogic().receiveCard(player.getId(), newCard.clone());
        } else if (cardLocation.equals(CardLocation.DECK)) {
            context.getLogic().shuffleToDeck(player, newCard.clone());
        }
        context.getLogic().remove(context.getOpponent(player).getId(), targetCard);
    }
}
