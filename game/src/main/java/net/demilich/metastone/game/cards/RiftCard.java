package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.cards.desc.PermanentCardDesc;
import net.demilich.metastone.game.cards.desc.RiftCardDesc;
import net.demilich.metastone.game.entities.minions.Permanent;
import net.demilich.metastone.game.entities.minions.Rift;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;

public class RiftCard extends PermanentCard {

    private int duration;
    private final PermanentCardDesc desc;

    public RiftCard(RiftCardDesc desc) {
        super(desc);
        duration = desc.duration;
        this.desc = desc;
    }

    public int getDuration() {
        return duration;
    }

    protected Rift createRift(Attribute... tags) {
        Rift rift = new Rift(this, duration);
        for (Attribute gameTag : getAttributes().keySet()) {
            if (!ignoredAttributes.contains(gameTag)) {
                rift.setAttribute(gameTag, getAttribute(gameTag));
            }
        }
        if (desc.deathrattle != null) {
            rift.removeAttribute(Attribute.DEATHRATTLES);
            rift.addDeathrattle(desc.deathrattle);
        }
        if (desc.trigger != null) {
            rift.addSpellTrigger(desc.trigger.create());
        }
        if (desc.triggers != null) {
            for (TriggerDesc trigger : desc.triggers) {
                rift.addSpellTrigger(trigger.create());
            }
        }
        if (desc.aura != null) {
            rift.addSpellTrigger(desc.aura.create());
            rift.setAttribute(Attribute.AURA, true);
        }
        if (desc.cardCostModifier != null) {
            rift.setCardCostModifier(desc.cardCostModifier.create());
        }
        return rift;
    }

    @Override
    public Rift summon() {
        return createRift();
    }
}
