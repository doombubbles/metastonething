package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class DamageReceivedTrigger extends GameEventTrigger {

	public DamageReceivedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		DamageEvent damageEvent = (DamageEvent) event;
		String customId = desc.getString(EventTriggerArg.CUSTOM);
		if (customId.equals("AMETHYST")) {
			if (damageEvent.getEventSource().getEntityType().equals(EntityType.CARD)) {
				if (((Card) damageEvent.getEventSource()).getCardType().equals(CardType.HERO_POWER)) {
					return false;
				}
			}
		}

		EntityType targetEntityType = (EntityType) desc.get(EventTriggerArg.TARGET_ENTITY_TYPE);
		if (targetEntityType != null && damageEvent.getVictim().getEntityType() != targetEntityType) {
			return false;
		}

		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.DAMAGE;
	}

}
