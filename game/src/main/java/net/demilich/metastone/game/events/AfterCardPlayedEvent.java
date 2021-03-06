package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class AfterCardPlayedEvent extends GameEvent {

	private final Card card;

	public AfterCardPlayedEvent(GameContext context, int playerId, Card card) {
		super(context, playerId, -1);
		this.card = card;
		context.setEventCard(card);
	}

	public Card getCard() {
		return card;
	}
	
	@Override
	public Entity getEventTarget() {
		return getCard();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.AFTER_PLAY_CARD;
	}
}
