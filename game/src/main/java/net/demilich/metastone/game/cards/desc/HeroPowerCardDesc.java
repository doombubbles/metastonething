package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.heroes.powers.HeroPower;
import net.demilich.metastone.game.heroes.powers.HeroPowerChooseOne;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;

public class HeroPowerCardDesc extends SpellCardDesc {
	
	public String[] options;
	public String bothOptions;
	public TriggerDesc trigger;

	@Override
	public Card createInstance() {
		if (options != null && options.length > 0) {
			return new HeroPowerChooseOne(this);
		}
		return new HeroPower(this);
	}

}
