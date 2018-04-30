package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.targeting.EntityReference;

public class DiscoverCardSpell extends Spell {
	
	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = SpellDesc.build(DiscoverCardSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);  
		return new SpellDesc(arguments);
	}
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList result = new CardList();
		boolean cannotReceiveOwned = desc.getBool(SpellArg.CANNOT_RECEIVE_OWNED);
		if (desc.contains(SpellArg.CARDS)) {
			for (Card card : SpellUtils.getCards(context, desc, player)) {
				if (!cannotReceiveOwned || !context.getLogic().hasCard(player, card)) {
					result.add(card);
				}
			}
		} else if (desc.contains(SpellArg.CARD_SOURCE)) {
			CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
			result = cardSource.getCards(context, player);
		}
		/*
		EntityFilter filter = desc.getEntityFilter();
		if (filter != null) {
			result.removeAll(card -> !filter.matches(context, player, card));
		}
		*/
		
		CardList cards = new CardList();
		
		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		for (int i = 0; i < count; i++) {
			if (!result.isEmpty()) {
				Card card = result.getRandom();
				cards.add(card);
				result.remove(card);
				if (context.getLogic().hasAttribute(player, Attribute.ALL_OPTIONS)) {
					SpellUtils.castChildSpell(context, player, ((SpellDesc) desc.get(SpellArg.SPELL)).addArg(SpellArg.CARD, card.getCardId()), source, target);
				}
			}
		}

		if (cards.isEmpty() || context.getLogic().hasAttribute(player, Attribute.ALL_OPTIONS)) {
			return;
		}

		SpellUtils.castChildSpell(context, player, SpellUtils.getDiscover(context, player, desc, cards).getSpell(), source, target);
	}

}
