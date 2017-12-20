package net.demilich.metastone.game.spells.desc.trigger;

import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;

import java.io.Serializable;

public class TriggerDesc implements Serializable {

	public EventTriggerDesc eventTrigger;
	public SpellDesc spell;
	public boolean oneTurn;
	public boolean oneTime;
	public boolean persistentOwner;
	public int turnDelay;

	public SpellTrigger create() {
		SpellTrigger trigger = new SpellTrigger(eventTrigger.create(), null, spell, oneTurn, turnDelay, oneTime);
		trigger.setPersistentOwner(persistentOwner);
		return trigger;
	}

}
