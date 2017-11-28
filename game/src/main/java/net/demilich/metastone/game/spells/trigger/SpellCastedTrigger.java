package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.AfterSpellCastedEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.SpellCastedEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class SpellCastedTrigger extends GameEventTrigger {

	public SpellCastedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		SpellCastedEvent spellCastedEvent = (SpellCastedEvent) event;

		String customId = desc.getString(EventTriggerArg.CUSTOM);
		if (customId == "MAGE_QUEST") {
			if (!spellCastedEvent.getSourceCard().hasAttribute(Attribute.RECEIVED) && !spellCastedEvent.getSourceCard().isCollectible()) {
				return false;
			}
		}

		Race race = (Race) desc.get(EventTriggerArg.RACE);
		if (race != null && spellCastedEvent.getSourceCard().getRace() != race) {
			return false;
		}
		
		EntityType targetEntityType = (EntityType) desc.get(EventTriggerArg.TARGET_ENTITY_TYPE);
		if (targetEntityType != null && (spellCastedEvent.getEventTarget() == null || targetEntityType != spellCastedEvent.getEventTarget().getEntityType())) {
			return false;
		}
		
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.SPELL_CASTED;
	}

}
