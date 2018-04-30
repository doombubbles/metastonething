package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.targeting.EntityReference;

public class DiscoverFilteredCardSpell extends Spell {
	
	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = SpellDesc.build(DiscoverFilteredCardSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);
		return new SpellDesc(arguments);
	}
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		EntityFilter[] cardFilters = (EntityFilter[]) desc.get(SpellArg.CARD_FILTERS);
		CardList cards = CardCatalogue.query(context.getDeckFormat());
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		if (cardSource != null) {
			cards = cardSource.getCards(context, player);
		}
		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		CardList discoverCards = new CardList();

		if (cardFilters != null) {
			for (EntityFilter filter : cardFilters) {
				CardList result = new CardList();
				for (Card card : cards) {
					if (filter == null || filter.matches(context, player, card)) {
						result.add(card);
					}
				}
				
				if (!result.isEmpty()) {
					discoverCards.add(result.getRandom());
				}
			}
		} else {
			CardList result = new CardList();
			for (Card card : cards) {
				if (cardFilter == null || cardFilter.matches(context, player, card)) {
					result.add(card);
				}
			}
			discoverCards = new CardList();
			
			for (int i = 0; i < count; i++) {
				if (!result.isEmpty()) {
					Card card = null;
					do {
						card = result.getRandom();
						result.remove(card);
					} while (discoverCards.containsCard(card));
					if (card != null) {
						discoverCards.add(card);

						if (context.getLogic().hasAttribute(player, Attribute.ALL_OPTIONS)) {
							SpellUtils.castChildSpell(context, player, ((SpellDesc) desc.get(SpellArg.SPELL)).addArg(SpellArg.CARD, card.getCardId()), source, target);
						}
					}
				}
			}
			
		}

		if (cards.isEmpty() || context.getLogic().hasAttribute(player, Attribute.ALL_OPTIONS)) {
			return;
		}

		SpellUtils.castChildSpell(context, player, SpellUtils.getDiscover(context, player, desc, cards).getSpell(), source, target);
	}

}
