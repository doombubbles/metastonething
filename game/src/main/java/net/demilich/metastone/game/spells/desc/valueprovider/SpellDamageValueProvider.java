package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class SpellDamageValueProvider extends ValueProvider {

    public SpellDamageValueProvider(ValueProviderDesc desc) {
        super(desc);
    }

    @Override
    protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
        int value = desc.getValue(ValueProviderArg.VALUE, 0);
        return context.getLogic().applySpellpower(player, host, value);
    }
}
