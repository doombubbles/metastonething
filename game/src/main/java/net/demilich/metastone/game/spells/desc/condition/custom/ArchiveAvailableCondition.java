package net.demilich.metastone.game.spells.desc.condition.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;

public class ArchiveAvailableCondition extends Condition {
    public ArchiveAvailableCondition(ConditionDesc desc) {
        super(desc);
    }

    @Override
    protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
        return player.getSupremeArchive() != null;
    }
}
