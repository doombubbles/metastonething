package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.MinionExistEvent;
import net.demilich.metastone.game.events.SummonEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class MinionExistsTrigger extends GameEventTrigger {

	public MinionExistsTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		MinionExistEvent existsEvent = (MinionExistEvent) event;
		Race race = (Race) desc.get(EventTriggerArg.RACE);
		if (race != null && (existsEvent.getMinion().getRace() != race && existsEvent.getMinion().getRace() != Race.ALL)) {
			return false;
		}

		Attribute requiredAttribute = (Attribute) desc.get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		if (requiredAttribute != null && !existsEvent.getMinion().hasAttribute(requiredAttribute)) {
			return false;
		}
		
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.MINION_EXISTS;
	}

}
