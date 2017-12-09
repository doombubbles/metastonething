package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public class MinionExistEvent extends GameEvent {

	private final Actor minion;
	private final Card source;

	public MinionExistEvent(GameContext context, Actor minion, Card source) {
		super(context, minion.getOwner(), -1);
		this.minion = minion;
		this.source = source;
	}
	
	@Override
	public Entity getEventTarget() {
		return getMinion();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.MINION_EXISTS;
	}

	public Actor getMinion() {
		return minion;
	}

	public Card getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "[Exist Event MINION " + minion + " from SOURCE " + source + "]";
	}

}
