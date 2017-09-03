package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.SecretCard;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

public class SecretCardDesc extends SpellCardDesc {

	public EventTriggerDesc trigger;
	public BattlecryDesc instant;
	
	@Override
	public Card createInstance() {
		return new SecretCard(this);
	}

}
