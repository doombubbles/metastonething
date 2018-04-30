package net.demilich.metastone.game.spells.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class ManaBindSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(ManaBindSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card targetCard = (Card) target;
		Card receiveCard = targetCard.getCopy();
		receiveCard.setAttribute(Attribute.MANA_COST_MODIFIER, -1 * (receiveCard.getAttributeValue(Attribute.BASE_MANA_COST)));
		context.getLogic().receiveCard(player.getId(), receiveCard);
	}

}
