package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.ChooseOneCard;
import net.demilich.metastone.game.entities.minions.Race;

public class ChooseOneCardDesc extends CardDesc {

	public String[] options;
	public String bothOptions;
	public Race race;

	@Override
	public Card createInstance() {
		return new ChooseOneCard(this);
	}

}
