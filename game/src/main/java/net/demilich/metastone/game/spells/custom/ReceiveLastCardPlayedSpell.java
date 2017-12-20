package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.ReceiveCardSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class ReceiveLastCardPlayedSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        if (target.getReference() == EntityReference.FRIENDLY_HERO) {
            context.getLogic().receiveCard(player.getId(), player.lastCardPlayed.clone());
        } else context.getLogic().receiveCard(player.getId(), context.getOpponent(player).lastCardPlayed.clone());
    }
}
