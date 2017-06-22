package net.demilich.metastone.game.spells.custom;

import java.util.Arrays;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class RogueQuestSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (player.getAttributeValue(Attribute.ROGUE_QUEST) < 1) {
			return;
		}
		MinionCard minion = player.lastPlayedMinion;
		if (player.roguequest.get(minion.getName()) != null) {
			player.roguequest.put(minion.getName(), player.roguequest.get(minion.getName()) + 1);
		} else player.roguequest.put(minion.getName(), 2);
		Object[] array = player.roguequest.values().toArray();
		Arrays.sort(array);
		player.setAttribute(Attribute.ROGUE_QUEST, array[array.length - 1]);
	}

}
