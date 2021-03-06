package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class AlgebraicValueProvider extends ValueProvider {

	private static int evaluateOperation(int value1, int value2, AlgebraicOperation operation) {
		return operation.performOperation(value1, value2);
	}

	public AlgebraicValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity source) {
		int value1 = desc.getValue(ValueProviderArg.VALUE_1, context, player, target, source, 1);
		int value2 = desc.getValue(ValueProviderArg.VALUE_2, context, player, target, source, 1);
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(ValueProviderArg.OPERATION);
		return evaluateOperation(value1, value2, operation);
	}

}
