package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public class AfterSummonEvent extends GameEvent {

	private final Actor minion;
	private final Card source;

	public AfterSummonEvent(GameContext context, Actor minion, Card source, GameContext previousContext) {
		super(context, minion.getOwner(), -1, previousContext);
		this.minion = minion;
		this.source = source;
		this.after = true;
	}
	
	@Override
	public Entity getEventTarget() {
		return getMinion();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.AFTER_SUMMON;
	}

	public Actor getMinion() {
		return minion;
	}

	public Card getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "[After Summon Event MINION " + minion + " from SOURCE " + source + "]";
	}

}
