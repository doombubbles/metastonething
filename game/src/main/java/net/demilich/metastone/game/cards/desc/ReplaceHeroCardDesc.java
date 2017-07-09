package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.ReplaceHeroCard;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;

public class ReplaceHeroCardDesc extends CardDesc {

	public String hero;
	public BattlecryDesc battlecry;
	public int armor;
	
	@Override
	public Card createInstance() {
		return new ReplaceHeroCard(this);
	}

}
