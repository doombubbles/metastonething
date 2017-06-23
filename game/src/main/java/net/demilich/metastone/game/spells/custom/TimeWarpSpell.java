package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.TurnEndEvent;
import net.demilich.metastone.game.events.TurnStartEvent;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class TimeWarpSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		context.getLogic().startTurn(player.getId());
	}

}
