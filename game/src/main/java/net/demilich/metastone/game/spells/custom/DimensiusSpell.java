package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.SpellCardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class DimensiusSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        SpellDesc realChildSpell = (SpellDesc) desc.get(SpellArg.SPELL);
        desc = desc.removeArg(SpellArg.SPELL);
        desc = desc.addArg(SpellArg.SPELL, NullSpell.create());
        CardCollection cards = player.getHand().clone();
        CardCollection gonnaDiscard = new CardCollection();
        cards.add(context.getCardById("finished_discarding"));
        Card card = null;
        String id = "";
        while (!id.equals("finished_discarding")) {
            if (card != null) {
                cards.remove(card);
                gonnaDiscard.add(card);
            }
            card = SpellUtils.getDiscover(context, player, desc, cards).getCard();
            id = card.getCardId();
        }
        for (Card card1 : gonnaDiscard) {
            context.getLogic().discardCard(player, (Card) context.resolveSingleTarget(card1.getReference()));
            SpellUtils.castChildSpell(context, player, realChildSpell, source, target);
        }
    }
}
