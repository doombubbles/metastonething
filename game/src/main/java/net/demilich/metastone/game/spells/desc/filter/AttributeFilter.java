package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.SpellUtils;

public class AttributeFilter extends EntityFilter {

	public AttributeFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity) {
		Attribute attribute = (Attribute) desc.get(FilterArg.ATTRIBUTE);
		ComparisonOperation operation = (ComparisonOperation) desc.get(FilterArg.OPERATION);
		if (operation == ComparisonOperation.HAS) {
			return entity.hasAttribute(attribute);
		}

		int targetValue = desc.getValue(FilterArg.VALUE, context, player, entity, entity, 0);
		
		int actualValue = -1;
		if (attribute == Attribute.ATTACK && entity.getEntityType() != EntityType.CARD) {
			if (entity instanceof Weapon) {
				actualValue = ((Weapon) entity).getWeaponDamage();
			} else {
				actualValue = ((Actor) entity).getAttack();
			}
		} else {
			actualValue = entity.getAttributeValue(attribute);
		}

		return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
	}

}
