package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

public class CastRecastSpell extends Spell{
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        int maxIterations = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
        SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
        Condition condition = (Condition) desc.get(SpellArg.CONDITION);
        for (int i = 0; i < maxIterations; i++) {
            SpellUtils.castChildSpell(context, player, spell, source, target);
            if (condition != null && !condition.isFulfilled(context, player, source, target)) {
                return;
            }
            context.getLogic().checkForDeadEntities();
        }
    }
}
