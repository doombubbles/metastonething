package net.demilich.metastone.game.spells.custom;

import java.util.Collection;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class Primalfin2Spell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (source.getEntityType() == EntityType.MINION) {
			Minion minion = (Minion) source;
			for (Card card : minion.primalfinchampion) {
				context.getLogic().receiveCard(player.getId(), card);
			}
		}
		
	}

}
