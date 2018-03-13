package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class EliseSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList ungoro = new CardList();
		CardList guarantee = new CardList();
		for (Card card : CardCatalogue.getAll()) {
			if (card.getCardSet() == CardSet.JOURNEY_TO_UNGORO && !card.getCardId().contains("quest") && card.isCollectible()) {
				ungoro.add(card);
				if (card.getRarity() == Rarity.EPIC || card.getRarity() == Rarity.LEGENDARY) {
					guarantee.add(card);
				}
			}
		}
		CardList cards = new CardList();
		cards.add(guarantee.getRandom());
		for (int i = 0; i < 4; i++) {
			cards.add(ungoro.getRandom());
		}
		cards.shuffle();
		for (Card card : cards) {
			card.setAttribute(Attribute.RECEIVED);
			context.getLogic().receiveCard(player.getId(), card, source);
		}
		
	}

}
