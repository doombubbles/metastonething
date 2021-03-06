package net.demilich.metastone.game.decks;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.decks.validation.DefaultDeckValidator;
import net.demilich.metastone.game.decks.validation.IDeckValidator;

public class RandomDeck extends Deck implements Serializable {
	
	private DeckFormat deckFormat;

	public RandomDeck(HeroClass heroClass, DeckFormat deckFormat) {
		super(heroClass);
		this.deckFormat = deckFormat;
		setName("[Random deck]");
	}

	@Override
	public CardList getCardsCopy() {
		Deck copyDeck = new Deck(getHeroClass());
		IDeckValidator deckValidator = new DefaultDeckValidator();
		CardList classCards = CardCatalogue.query(deckFormat, card -> {
			return card.isCollectible() && !card.getCardType().isCardType(CardType.HERO) && !card.getCardType().isCardType(CardType.HERO_POWER) && card.hasHeroClass(getHeroClass());
		});
		CardList neutralCards = CardCatalogue.query(deckFormat, card -> {
			return card.isCollectible() && !card.getCardType().isCardType(CardType.HERO) && !card.getCardType().isCardType(CardType.HERO_POWER) && card.hasHeroClass(HeroClass.ANY);
		});

		while (!copyDeck.isComplete()) {
			// random deck consists of roughly 50% class cards and 50% neutral
			// cards
			Card randomCard = ThreadLocalRandom.current().nextBoolean() ? classCards.getRandom() : neutralCards.getRandom();
			if (deckValidator.canAddCardToDeck(randomCard, copyDeck)) {
				copyDeck.getCards().add(randomCard);
			}
		}
		return copyDeck.getCardsCopy();
	}

}
