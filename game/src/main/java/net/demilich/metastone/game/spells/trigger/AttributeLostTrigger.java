package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.AttributeLostEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class AttributeLostTrigger extends GameEventTrigger {

	public AttributeLostTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		AttributeLostEvent coolEvent = (AttributeLostEvent) event;
		Attribute attr = (Attribute) desc.get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		return coolEvent.getEventAttribute().equals(attr);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.ATTRIBUTE_LOST;
	}

}
