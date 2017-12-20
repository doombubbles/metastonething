package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.TargetAcquisitionEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

public class AttackSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        Entity attackSource = source;
        if (desc.contains(SpellArg.SPELL_SOURCE)) {
            if ((EntityReference) desc.get(SpellArg.SPELL_SOURCE) == EntityReference.TARGET) {
                attackSource = target;
            } else attackSource = context.resolveSingleTarget((EntityReference) desc.get(SpellArg.SPELL_SOURCE));
        }
        Actor attackTarget = (Actor) target;
        if (desc.contains(SpellArg.SECONDARY_TARGET)) {
            List<Entity> targets = context.resolveTarget(player, attackSource, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET));
            /*
            if (desc.contains(SpellArg.EXCLUSIVE)) {
                targets.removeIf(entity -> entity.getEntityType().equals(EntityType.HERO) && entity.getOwner() == player.getId());
            }
            */
            attackTarget = (Actor) targets.get(context.getLogic().random(targets.size()));
        }
        if (!attackSource.hasAttribute(Attribute.FROZEN)) {
            context.getLogic().fight(player, (Actor) attackSource, attackTarget);
        }
    }
}
