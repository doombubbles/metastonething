package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SummonRandomNotOnBoardSpell extends Spell {

	private static boolean alreadyOnBoard(List<Summon> summons, String id) {
		for (Summon summon : summons) {
			if (summon.getSourceCard().getCardId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = desc.getValue(SpellArg.VALUE, 1);
		int howMany = desc.getValue(SpellArg.HOW_MANY, 1);
		String[] minionCardsId = (String[]) desc.get(SpellArg.CARDS);
		List<String> eligibleMinions = new ArrayList<String>();
		for (String minion : minionCardsId) {
			if (!alreadyOnBoard(player.getSummons(), minion)) {
				eligibleMinions.add(minion);
			}
		}
		for (int i = 1; i <= value; i++) {
			if (eligibleMinions.isEmpty()) {
				return;
			}
			String randomMinionId = eligibleMinions.get(context.getLogic().random(eligibleMinions.size()));
			MinionCard randomMinionCard = (MinionCard) context.getCardById(randomMinionId);
			for (int j = 1; j <= howMany; j++) {
				context.getLogic().summon(player.getId(), randomMinionCard.summon());
			}
			eligibleMinions.remove(randomMinionId);
		}

	}

}
