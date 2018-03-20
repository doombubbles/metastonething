package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

public class CardCounter extends ValueProvider {

	public CardCounter(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity source) {
		if (desc.contains(ValueProviderArg.TARGET)) {
			switch (((EntityReference) desc.get(ValueProviderArg.TARGET)).getId()) {
				case -17:
					return context.getOpponent(player).getHand().getCount();
				case -78:
					return player.getDeck().getCount();
				case -79:
					return context.getOpponent(player).getDeck().getCount();
			}
		}
		return player.getHand().getCount();

	}

}
