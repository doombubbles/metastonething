package net.demilich.metastone.game.spells.desc.filter;

import java.util.List;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

public class HighestAttributeFilter extends EntityFilter {

	public HighestAttributeFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity source) {
		Attribute attribute = (Attribute) desc.get(FilterArg.ATTRIBUTE);
		EntityReference targetReference = (EntityReference) desc.get(FilterArg.TARGET);
		List<Entity> entities = context.resolveTarget(player, entity, targetReference);
		if (desc.contains(FilterArg.INVERT)) {
			int lowest = getLowestInList(entities, attribute);
			return getAttributeValue(entity, attribute) <= lowest;
		} else {
			int highest = getHighestInList(entities, attribute);
			return getAttributeValue(entity, attribute) >= highest;
		}
	}

	private static int getAttributeValue(Entity entity, Attribute attribute) {
		if (attribute == Attribute.ATTACK) {
			return ((Actor) entity).getAttack();
		}
		return entity.getAttributeValue(attribute);
	}

	private static int getHighestInList(List<Entity> entities, Attribute attribute) {
		int highest = Integer.MIN_VALUE;
		for (Entity entity : entities) {
			int attributeValue = getAttributeValue(entity, attribute);
			if (attributeValue > highest) {
				highest = attributeValue;
			}
		}
		return highest;
	}

	private static int getLowestInList(List<Entity> entities, Attribute attribute) {
		int lowest = Integer.MAX_VALUE;
		for (Entity entity : entities) {
			int attributeValue = getAttributeValue(entity, attribute);
			if (attributeValue < lowest) {
				lowest = attributeValue;
			}
		}
		return lowest;
	}

}
