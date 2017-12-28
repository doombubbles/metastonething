package net.demilich.metastone.game.spells.desc.condition.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextlessCondition extends Condition {
    public TextlessCondition(ConditionDesc desc) {
        super(desc);
    }

    @Override
    protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
        List<Entity> targets = context.resolveTarget(player, source, target.getReference());
        targets.forEach(entity -> {
            if (!entity.getEntityType().equals(EntityType.CARD)) {
                return;
            }
        });
        List<Card> cards = new ArrayList<>();
        targets.forEach(entity -> cards.add((Card) entity));
        for (Card card : cards) {
            if (card.getCardType().equals(CardType.MINION)) {
                if (!card.getDescription().equals("") && !card.getName().equals("Fifi Fizzlewarp")) {
                    return false;
                }
            }
        }
        return true;
    }
}
