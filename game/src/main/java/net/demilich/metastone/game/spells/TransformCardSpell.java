package net.demilich.metastone.game.spells;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.CardLocation;

import java.util.List;

public class TransformCardSpell extends Spell {

	public static Logger logger = LoggerFactory.getLogger(TransformCardSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = (Card) target;
		if (card.getLocation() == CardLocation.HAND) {
			//context.getLogic().removeCardFromHand(player.getId(), card);
		} else {
			// logger.warn("Trying to transform card {} in invalid location {}",
			// card, card.getLocation());
			return;
		}
		Card newCard = null;
		if (desc.get(SpellArg.CARD) != null) {
			newCard = context.getCardById((String) desc.get(SpellArg.CARD));
		} else if (desc.get(SpellArg.CARDS) != null){
			String[] cards = (String[]) desc.get(SpellArg.CARDS);
			newCard = context.getCardById(cards[context.getLogic().random(cards.length)]);
		}

		//context.getLogic().receiveCard(player.getId(), newCard);
		context.getLogic().replaceCard(player.getId(), card, newCard);
	}

}
