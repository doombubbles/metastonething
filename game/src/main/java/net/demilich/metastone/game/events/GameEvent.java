package net.demilich.metastone.game.events;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.GameEventTrigger;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;

import java.io.Serializable;

public abstract class GameEvent implements Serializable {

	private final GameContext context;
	private final int targetPlayerId;
	private final int sourcePlayerId;
	private final int value;

	public GameEvent(GameContext context, int targetPlayerId, int sourcePlayerId, int value) {
		this.context = context;
		this.targetPlayerId = targetPlayerId;
		this.sourcePlayerId = sourcePlayerId;
		this.value = value;
	}

	public GameEvent(GameContext context, int targetPlayerId, int sourcePlayerId) {
		this.context = context;
		this.targetPlayerId = targetPlayerId;
		this.sourcePlayerId = sourcePlayerId;
		this.value = 0;
	}

	/**
	 * Spells may specify to be cast on the event target; this is dependent on
	 * the actual event. For example, a SummonEvent may return the summoned
	 * minion, a DamageEvent may return the damaged minion/hero, etc.
	 * 
	 * @return
	 */
	public abstract Entity getEventTarget();
	
	public Entity getEventSource() {
		return null;
	}
	
	public abstract GameEventType getEventType();

	public GameContext getGameContext() {
		return context;
	}

	public int getTargetPlayerId() {
		return targetPlayerId;
	}
	
	public int getSourcePlayerId() {
		return sourcePlayerId;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "[EVENT " + getClass().getSimpleName() + "]";
	}
	
	public void updateQuests(GameContext context) {
		Player player1 = context.getPlayer1();
		if (player1.getQuests().size() >= 1 && !context.gameDecided()) {
			int counter = ((SpellTrigger) context.getLogic().getQuests(player1).get(0)).getPrimaryCount();
			player1.setAttribute(Attribute.QUEST, counter);
		}
		Player player2 = context.getPlayer2();
		if (player2.getQuests().size() >= 1 && !context.gameDecided()) {
			int counter = ((SpellTrigger) context.getLogic().getQuests(player2).get(0)).getPrimaryCount();
			player2.setAttribute(Attribute.QUEST, counter);
			
		}
	}
}
