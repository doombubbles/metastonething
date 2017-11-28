package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.AddDeathrattleSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Map;

public class SeepingSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        Card woah;
        for (Card card : player.getDeck().clone().shuffle()) {
            if (card.getCardType().equals(CardType.MINION) && card.hasAttribute(Attribute.DEATHRATTLES)) {
                context.fireGameEvent(new CardRevealedEvent(context, player.getId(), card, 0));
                SpellDesc spell = AddDeathrattleSpell.create(((MinionCard) card).summon().getDeathrattles().get(0));
                SpellUtils.castChildSpell(context, player, spell, source, source);
                break;
            }
        }
    }
}
