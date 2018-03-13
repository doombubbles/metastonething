package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class MuklaSpell extends Spell {
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int bananas = 0;
		CardList cards1 = new CardList();
		CardList cards2 = new CardList();
		for (Card card : player.getHand()) {
			if (card.getCardId() == CardCatalogue.getCardById("spell_bananas").getCardId()) {
				cards1.add(card);
				bananas += 2;
			}
		}
		for (Card card : context.getOpponent(player).getHand()) {
			if (card.getCardId() == CardCatalogue.getCardById("spell_bananas").getCardId()) {
				cards2.add(card);
				bananas += 2;
			}
		}
		for (Card card : cards1) { 
			context.getLogic().discardCard(player, card);
		}
		for (Card card : cards2) { 
			context.getLogic().discardCard(context.getOpponent(player), card);
		}
		bananas /= 2;
		for (int i = 0; i < bananas; i++) {
			SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
			SpellUtils.castChildSpell(context, player, spell, source, target);
		}
	}

}
