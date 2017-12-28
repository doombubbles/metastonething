package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;

public class LowestAttributeValueProvider extends ValueProvider {

	public LowestAttributeValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		EntityReference sourceReference = (EntityReference) desc.get(ValueProviderArg.TARGET);
		Attribute attribute = (Attribute) desc.get(ValueProviderArg.ATTRIBUTE);
		List<Entity> entities = null;
		if (sourceReference != null) {
			entities = context.resolveTarget(player, host, sourceReference);
		} else {
			entities = new ArrayList<>();
			entities.add(target);
		}
		if (entities == null) {
			return 0;
		}

		EntityFilter filter = (EntityFilter) desc.get(ValueProviderArg.FILTER);		
		int value = 0;
		for (Entity entity : entities) {
			if (filter != null && !filter.matches(context, player, entity)) {
				continue;
			}
			if (entity instanceof Card) {
				Card card = (Card) entity;
				if (attribute == Attribute.ATTACK) {
					value = Math.min(card.getAttributeValue(Attribute.ATTACK) + card.getAttributeValue(Attribute.ATTACK_BONUS), value);
				} else if (attribute == Attribute.MAX_HP) {
					value = Math.min(card.getAttributeValue(Attribute.MAX_HP) + card.getAttributeValue(Attribute.HP_BONUS), value);
				} else {
					value = Math.max(card.getAttributeValue(attribute), value);
				}
			} else {
				Actor source = (Actor) entity;
				if (attribute == Attribute.ATTACK) {
					value = Math.min(source.getAttack(), value);
				} else if (attribute == Attribute.MAX_HP) {
					value = Math.min(source.getMaxHp(), value);
				} else {
					value = Math.min(source.getAttributeValue(attribute), value);
				}
			}
		}

		return value;
	}

}
