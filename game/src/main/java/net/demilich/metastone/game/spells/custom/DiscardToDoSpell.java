package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class DiscardToDoSpell extends Spell {
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String ID = desc.getString(SpellArg.CARD);
		for (Card card : player.getHand()) {
			if (card.getCardId() == CardCatalogue.getCardById(ID).getCardId()) {
				context.getLogic().discardCard(player, card);
				SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
				SpellUtils.castChildSpell(context, player, spell, source, target);
				break;
			}
		}
		
	}

}
