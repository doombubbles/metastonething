package net.demilich.metastone.game.cards;

public enum CardType {
	CHOOSE_ONE,
	GROUP,
	HERO,
	HERO_POWER,
	MINION,
	PERMANENT,
	SPELL,
	WEAPON,
	REPLACE_HERO,
	RIFT,
	;
	
	public boolean isCardType(CardType cardType) {
		if (this == CHOOSE_ONE && cardType == SPELL) {
			return true;
		} else if (this == cardType) {
			return true;
		}
		return false;
	}
}
