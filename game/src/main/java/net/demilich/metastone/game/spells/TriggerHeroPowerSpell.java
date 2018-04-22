package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

public class TriggerHeroPowerSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        SpellDesc heroPowerSpell = player.getHero().getHeroPower().getSpell();
        boolean hasTarget = heroPowerSpell.hasPredefinedTarget();
        if (!hasTarget) {
            List<Entity> targets = context.getLogic().getValidTargets(player.getId(), context.getLogic().getAutoHeroPowerAction(player.getId()));
            target = SpellUtils.getRandomTarget(targets);
        }
        SpellUtils.castChildSpell(context, player, heroPowerSpell, source, target);
        context.getLogic().useHeroPower(player.getId(), false);
    }
}
