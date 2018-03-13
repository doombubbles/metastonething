package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;

public class AlgebraicFilter extends CardFilter {

    public AlgebraicFilter(FilterDesc desc) {
        super(desc);
    }

    @Override
    protected boolean test(GameContext context, Player player, Entity entity, Entity source) {
        ComparisonOperation operation = (ComparisonOperation) desc.get(FilterArg.OPERATION);
        AlgebraicOperation algebraicOperation = (AlgebraicOperation) desc.get(FilterArg.ALGEBRAIC_OPERATION);

        int value1 = desc.getValue(FilterArg.VALUE1, context, player, entity, entity, 0);
        int value2 = desc.getValue(FilterArg.VALUE2, context, player, entity, entity, 0);
        int targetValue = desc.getValue(FilterArg.VALUE, context, player, entity, entity, 0);

        int actualValue = algebraicOperation.performOperation(value1, value2);

        return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
    }
}
