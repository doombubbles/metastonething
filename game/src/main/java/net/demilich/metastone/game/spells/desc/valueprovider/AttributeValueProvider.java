package net.demilich.metastone.game.spells.desc.valueprovider;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Rift;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

public class AttributeValueProvider extends ValueProvider {

	public AttributeValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		EntityReference sourceReference = (EntityReference) desc.get(ValueProviderArg.TARGET);
		Attribute attribute = (Attribute) desc.get(ValueProviderArg.ATTRIBUTE);
		EntityFilter filter = (EntityFilter) desc.get(ValueProviderArg.FILTER);
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
		int value = 0;

		for (Entity entity : entities) {
			if (filter != null) {
				if (!filter.matches(context, player, entity, host)) {
					continue;
				}
			}

			if (entity.getEntityType().equals(EntityType.CARD)) {
				Card card = (Card) entity;
				value += card.getAttributeValue(attribute);
				if (desc.getString(ValueProviderArg.CUSTOM).equals("HIGHLANDER")) {
					return value;
				}
			} else {
				if (entity instanceof Actor) {
					Actor source = (Actor) entity;
					if (attribute == Attribute.ATTACK) {
						value += source.getAttack();
					} else if (attribute == Attribute.MAX_HP) {
						value += source.getMaxHp();
					} else {
						value += source.getAttributeValue(attribute);
					}
				} else {
					value += entity.getAttributeValue(attribute);
				}
			}
		}

		return value;
	}

}
