package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;

import java.io.Serializable;

public class Deck implements Serializable {

	private String name = "";
	private final HeroClass heroClass;
	private final CardList cards = new CardList();
	private String description;
	private String filename;
	private boolean arbitrary;

	public Deck(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public Deck(HeroClass heroClass, boolean arbitrary) {
		this.heroClass = heroClass;
		this.arbitrary = arbitrary;
	}

	public int containsHowMany(Card card) {
		int count = 0;
		for (Card cardInDeck : cards) {
			if (card.getCardId().equals(cardInDeck.getCardId())) {
				count++;
			}
		}
		return count;
	}

	public CardList getCards() {
		return cards;
	}

	public CardList getCardsCopy() {
		return getCards().clone();
	}

	public String getDescription() {
		return description;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public String getName() {
		return name;
	}
	
	public boolean isArbitrary() {
		return arbitrary;
	}

	public boolean isComplete() {
		return cards.getCount() == GameLogic.DECK_SIZE;
	}
	
	public boolean isFull() {
		return cards.getCount() == GameLogic.MAX_DECK_SIZE;
	}

	public boolean isMetaDeck() {
		return getHeroClass() == HeroClass.DECK_COLLECTION;
	}
	
	public boolean isTooBig() {
		return cards.getCount() > GameLogic.DECK_SIZE;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
