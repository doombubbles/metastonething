package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class BarnabusSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList deck = player.getDeck();
		for (Card card : deck) {
			if (card.getCardType() == CardType.MINION) {
				card.setAttribute(Attribute.MANA_COST_MODIFIER, card.getBaseManaCost() * -1);
			}
		}
		player.getDeck().equals(deck);
		
	}

}
