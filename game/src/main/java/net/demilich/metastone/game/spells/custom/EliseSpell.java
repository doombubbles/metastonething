package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class EliseSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardCollection ungoro = new CardCollection();
		for (Card card : CardCatalogue.getAll()) {
			if (card.getCardSet() == CardSet.JOURNEY_TO_UNGORO && !card.getCardId().contains("quest") && card.isCollectible()) {
				ungoro.add(card);/*
				if (card.getRarity() == Rarity.LEGENDARY) {
					ungoro.add(card);
				}
				*/
			}
		}
		
		CardCollection cards = new CardCollection();
		cards.add(ungoro.getRandom());
		cards.add(ungoro.getRandom());
		cards.add(ungoro.getRandom());
		cards.add(ungoro.getRandom());
		cards.add(ungoro.getRandom());
		
		int i = 0;
		
		for (Card card : ungoro) {
			if (card.getRarity() == Rarity.EPIC || card.getRarity() == Rarity.LEGENDARY) {
				i += 1;
			}
		}
		
		while (i < 1) {
			cards.removeFirst();
			Card card = ungoro.getRandom();
			if (card.getRarity() == Rarity.EPIC || card.getRarity() == Rarity.LEGENDARY) {
				i += 1;
			}
			cards.add(card);
		}
		
		
		int avail = 10 - player.getHand().getCount();
		while (avail < cards.getCount()) {
			cards.removeFirst();
		}
		for (Card card : cards) {
			card.received = true;
			context.getLogic().receiveCard(player.getId(), card, source);
		}
		
	}

}
