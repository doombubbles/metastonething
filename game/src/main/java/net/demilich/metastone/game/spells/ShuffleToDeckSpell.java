package net.demilich.metastone.game.spells;

import java.util.HashMap;
import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;

public class ShuffleToDeckSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = null;
		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		if (target != null && target.getEntityType() != EntityType.CARD) {
			card = ((Actor) target).getSourceCard().getCopy();
		} else if (desc.contains(SpellArg.CARD_FILTER)){
			EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
			CardList cards = CardCatalogue.query(context.getDeckFormat());
			CardList result = new CardList();
			for (Card cardResult : cards) {
				if (cardFilter.matches(context, player, cardResult)) {
					result.add(cardResult);
				}
			}
			card = result.getRandom();
		} else if (target != null && target.getEntityType() == EntityType.CARD) {
			card = (Card) target;
		} else if (desc.contains(SpellArg.CARD_SOURCE)) {
			CardList cards = ((CardSource) desc.get(SpellArg.CARD_SOURCE)).getCards(context, player);
			Map<Card, Integer> gonnaShuffle = new HashMap<Card, Integer>();
			for (Card iCard : cards) {
				if (!gonnaShuffle.containsKey(iCard)) {
					gonnaShuffle.put(iCard, 1);
				} else if (gonnaShuffle.get(iCard) < howMany) {
					gonnaShuffle.put(iCard, gonnaShuffle.get(iCard) + 1);
				}
			}
			
			gonnaShuffle.forEach((iCard, value) -> {
				for (int i = 0; i < value; i++) {
					context.getLogic().shuffleToDeck(player, context.getCardById(iCard.getCardId()));
				}
			});
		}
		else {
			card = SpellUtils.getCard(context, desc);				
		}
		for (int i = 0; i < howMany; i++) {
			if (card != null) {
				Card newCard = context.getCardById(card.getCardId());
				if (spell != null) {
					SpellUtils.castChildSpell(context, player, spell, source, newCard);
				}
				context.getLogic().shuffleToDeck(player, newCard);
			}
		}
	}

}
