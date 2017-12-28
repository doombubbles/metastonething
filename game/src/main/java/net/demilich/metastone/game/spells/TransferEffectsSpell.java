package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;

public class TransferEffectsSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        List<Entity> realTargets = context.resolveTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET));
        EntityFilter entityFilter = desc.contains(SpellArg.CARD_FILTER) ? (EntityFilter) desc.get(SpellArg.CARD_FILTER) : null;
        List<Entity> realRealTargets = new ArrayList<>();
        if (entityFilter != null) {
            for (Entity entity : realTargets) {
                if (entityFilter.matches(context, player, entity)) {
                    realRealTargets.add(entity);
                }
            }
        } else realRealTargets = realTargets;
        for (Entity entity : realRealTargets) {
            if (target.hasAttribute(Attribute.DEATHRATTLES)) {
                entity.setAttribute(Attribute.DEATHRATTLES, target.getAttribute(Attribute.DEATHRATTLES));
            }
            for (IGameEventListener trigger : context.getTriggersAssociatedWith(target.getReference())) {
                IGameEventListener triggerClone = trigger.clone();
                context.getLogic().addGameEventListener(player, triggerClone, entity);
            }
        }
    }
}
