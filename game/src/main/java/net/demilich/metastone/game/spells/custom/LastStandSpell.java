package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class LastStandSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        SpellDesc spellDesc = desc.contains(SpellArg.SPELL) ? (SpellDesc) desc.get(SpellArg.SPELL) : null;
        CardList deck = player.getDeck().clone();
        MinionCard minionCard = null;
        for (int i = deck.getCount() - 1; i >= 0; i--) {
            if (deck.get(i) instanceof MinionCard) {
                minionCard = (MinionCard) deck.get(i);
            }
        }
        if (spellDesc == null || minionCard == null) {
            return;
        }
        SpellUtils.castChildSpell(context, player, spellDesc, source, minionCard);
    }
}
