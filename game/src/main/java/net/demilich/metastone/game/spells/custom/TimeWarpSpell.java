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
		/*
		context.fireGameEvent(new TurnEndEvent(context, player.getId()));
		player.setMaxMana(Math.min(player.getMaxMana() + 1, 10));
		player.setMana(player.getMaxMana());
		context.getLogic().drawCard(player.getId(), source);
		context.fireGameEvent(new TurnStartEvent(context, player.getId()));
		for (Minion minion : player.getMinions()) {
			minion.removeAttribute(Attribute.SUMMONING_SICKNESS);
			
		}
		*/
		context.getLogic().startTurn(player.getId());
	}

}
