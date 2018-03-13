package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

public class DiscardSpell extends Spell {

	public static final int ALL_CARDS = -1;

	public static SpellDesc create() {
		return create(1);
	}

	public static SpellDesc create(int numberOfCards) {
		Map<SpellArg, Object> arguments = SpellDesc.build(DiscardSpell.class);
		arguments.put(SpellArg.VALUE, numberOfCards);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		int numberOfCards = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);

		CardList discardableCards = new CardList();
		for (Card card : player.getHand()) {
			if (cardFilter == null || cardFilter.matches(context, player, card)) {
				discardableCards.add(card);
			}
		}
		
		int cardCount = numberOfCards == ALL_CARDS ? discardableCards.getCount() : numberOfCards;

		for (int i = 0; i < cardCount; i++) {
			Card randomHandCard = discardableCards.getRandom();
			if (randomHandCard == null) {
				return;
			}
			player.getDiscarded().add(randomHandCard.clone());
			context.getLogic().discardCard(player, randomHandCard);
			discardableCards.remove(randomHandCard);
			context.fireGameEvent(new CardRevealedEvent(context, context.getPlayer1().getId(), randomHandCard.clone(), (1 + i) * 1.2));
			context.fireGameEvent(new CardRevealedEvent(context, context.getPlayer2().getId(), randomHandCard.clone(), (1 + i) * 1.2));
			
		}
	}

}
