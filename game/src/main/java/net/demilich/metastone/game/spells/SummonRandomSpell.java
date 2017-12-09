package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SummonRandomSpell extends Spell {

	public static SpellDesc create(MinionCard... minionCards) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonRandomSpell.class);
		arguments.put(SpellArg.CARDS, minionCards);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String[] minionCardsId = (String[]) desc.get(SpellArg.CARDS);
		CardCollection cards = new CardCollection();
		for (String s : minionCardsId) {
			cards.add(context.getCardById(s));
		}
		for (int i = 1; i <= desc.getValue(SpellArg.VALUE, context, player, target, source, 1); i++ ) {
			if (cards.isEmpty()) {
				return;
			}
			Card randomMinionCard = cards.getRandom();
			context.getLogic().summon(player.getId(), ((MinionCard)randomMinionCard).summon());
			if (desc.contains(SpellArg.EXCLUSIVE)) {
				cards.remove(randomMinionCard);
			}
		}

	}

}
