package net.demilich.metastone.game.spells.custom;

import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.trigger.CardPlayedTrigger;
import net.demilich.metastone.game.spells.trigger.TurnStartTrigger;
import net.demilich.metastone.game.targeting.TargetType;

public class ShadowReflectionSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = (Card) target;
		Card newCard = context.getCardById(context.getEventCard().getCardId());
		newCard.setAttribute(Attribute.ONE_TURN, true);
		newCard.setName("'" + newCard.getName() + "'");
		newCard.setAttribute(Attribute.ATTACK_BONUS, context.getPendingCard().getAttribute(Attribute.ATTACK_BONUS) != null ? context.getPendingCard().getAttributeValue(Attribute.ATTACK_BONUS) : 0);
		newCard.setAttribute(Attribute.HP_BONUS, context.getPendingCard().getAttribute(Attribute.HP_BONUS) != null ? context.getPendingCard().getAttributeValue(Attribute.HP_BONUS) : 0);
		context.getLogic().replaceCard(player.getId(), card, newCard);
		
		Map<EventTriggerArg, Object> arguments = EventTriggerDesc.build(CardPlayedTrigger.class);
		arguments.put(EventTriggerArg.TARGET_PLAYER, TargetPlayer.SELF);
		arguments.put(EventTriggerArg.HOST_TARGET_TYPE, TargetType.IGNORE_AS_TARGET);
		EventTriggerDesc eventTriggerDesc = new EventTriggerDesc(arguments);
		
		TriggerDesc triggerDesc = new TriggerDesc();
		triggerDesc.eventTrigger = eventTriggerDesc;
		triggerDesc.spell = desc;
		
		context.getLogic().addGameEventListener(player, triggerDesc.create(), newCard);
	}

}
