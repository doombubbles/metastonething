package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;

import java.util.List;

public class TriggerValueProvider extends ValueProvider {
    public TriggerValueProvider(ValueProviderDesc desc) {
        super(desc);
    }

    @Override
    protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
        List<IGameEventListener> triggers = context.getTriggers();
        for (IGameEventListener trigger : triggers) {
            if (trigger instanceof SpellTrigger) {
                SpellTrigger spellTrigger = (SpellTrigger) trigger;
                String name = desc.getString(ValueProviderArg.CUSTOM);
                if (spellTrigger.getHostReference().equals(host.getReference())) {
                    if (spellTrigger.getPrimaryTrigger().interestedIn().toString().equals(name)) {
                        return spellTrigger.getPrimaryCount();
                    }
                }
            } else continue;


        }
        return 0;
    }
}
