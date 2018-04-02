package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.AfterCardPlayedEvent;
import net.demilich.metastone.game.events.CardPlayedEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class AfterCardPlayedTrigger extends GameEventTrigger {

	public AfterCardPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		AfterCardPlayedEvent afterCardPlayedEvent = (AfterCardPlayedEvent) event;
		CardType cardType = (CardType) desc.get(EventTriggerArg.CARD_TYPE);
		if (cardType != null && !afterCardPlayedEvent.getCard().getCardType().isCardType(cardType)) {
			return false;
		}
		
		Race race = (Race) desc.get(EventTriggerArg.RACE);
		if (race != null && afterCardPlayedEvent.getCard().getAttribute(Attribute.RACE) != race) {
			if (afterCardPlayedEvent.getCard().getAttribute(Attribute.RACE) != Race.ALL) {
				return false;
			}
		}
		
		Attribute attribute = (Attribute) desc.get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		if (attribute != null && afterCardPlayedEvent.getCard().hasAttribute(attribute) != true) {
			return false;
		}


		
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.AFTER_PLAY_CARD;
	}

}
