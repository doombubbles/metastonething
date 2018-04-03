package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.CardPlayedEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardPlayedTrigger extends GameEventTrigger {

	public CardPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		CardPlayedEvent cardPlayedEvent = (CardPlayedEvent) event;
		CardType cardType = (CardType) desc.get(EventTriggerArg.CARD_TYPE);
		if (cardType != null && !cardPlayedEvent.getCard().getCardType().isCardType(cardType)) {
			return false;
		}
		
		Race race = (Race) desc.get(EventTriggerArg.RACE);
		if (race != null && cardPlayedEvent.getCard().getAttribute(Attribute.RACE) != race) {
			if (cardPlayedEvent.getCard().getAttribute(Attribute.RACE) != Race.ALL) {
				return false;
			}
		}
		
		Attribute attribute = (Attribute) desc.get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		if (attribute != null && cardPlayedEvent.getCard().hasAttribute(attribute) != true) {
			return false;
		}
		
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.PLAY_CARD;
	}

}
