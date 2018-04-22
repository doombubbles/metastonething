package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.List;

public class HatSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
        int howMany = desc.getInt(SpellArg.HOW_MANY);
        List<Entity> entities = SpellUtils.getValidTargets(context, player, context.resolveTarget(player, source, desc.getTarget()), null);
        List<Entity> targets = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            if (!entities.isEmpty()) {
                Entity entity = SpellUtils.getRandomTarget(entities);
                targets.add(entity);
                entities.remove(target);
            }
        }

        for (Entity entity : targets) {
            SpellUtils.castChildSpell(context, player, spell, source, entity);
        }
    }
}
