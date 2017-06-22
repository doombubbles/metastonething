package net.demilich.metastone.game.events;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;

public class CardPlayedEvent extends GameEvent {

	private final Card card;

	public CardPlayedEvent(GameContext context, int playerId, Card card) {
		super(context, playerId, -1);
		this.card = card;
		
		if (card.getCardType() == CardType.MINION) {
			if (card.getRace() == Race.ELEMENTAL || card.getRace() == Race.ALL) {
				Player owner = context.getPlayer(card.getOwner());
				owner.modifyAttribute(Attribute.ELEMENTALS_THIS_TURN, 1);
			}
			
			context.getPlayer(card.getOwner()).lastPlayedMinion = (MinionCard) card;
		}
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
		return GameEventType.PLAY_CARD;
	}
}
