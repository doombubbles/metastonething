package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class DynoSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardCollection minions = new CardCollection();
		for (Card card : player.getDiscarded()) {
			if (card.getCardType() == CardType.MINION) {
				minions.add(card);
			}
		}
		MinionCard minion = (MinionCard) minions.getRandom();
		if (minion == null) {
			return;
		}
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		context.getLogic().summon(player.getId(), minion.summon(), null, boardPosition, false);
	}

}
