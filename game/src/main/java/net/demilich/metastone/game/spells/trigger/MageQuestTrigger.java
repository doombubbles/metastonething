package net.demilich.metastone.game.spells.trigger;

import javax.smartcardio.Card;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.AfterSpellCastedEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.SpellCastedEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class MageQuestTrigger extends GameEventTrigger {

	public MageQuestTrigger(EventTriggerDesc desc) {
		super(desc);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		SpellCard card = (SpellCard) event.getEventSource();
 		if ((card.hasAttribute(Attribute.RECEIVED)|| !card.isCollectible()) && event.getGameContext().getActivePlayerId() == event.getSourcePlayerId()) {
			return true;
		} else return false;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.SPELL_CASTED;
	}
	
}
