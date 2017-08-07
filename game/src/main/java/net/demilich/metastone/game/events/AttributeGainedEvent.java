package net.demilich.metastone.game.events;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class AttributeGainedEvent extends GameEvent {
	
	private final Entity target;
	private final Attribute attribute;

	public AttributeGainedEvent(GameContext context, Entity target, Attribute attribute) {
		super(context, target.getOwner(), -1);
		this.target = target;
		this.attribute = attribute;
	}

	@Override
	public Entity getEventTarget() {
		return target;
	}
	
	public Attribute getEventAttribute() {
		return attribute;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.ATTRIBUTE_GAINED;
	}

}
