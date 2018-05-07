package net.demilich.metastone.game.spells;

import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardPlayedEvent;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import java.util.Map;

public class ReplayCardsSpell extends Spell {


    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardList playedCards = new CardList();
        EntityFilter filter = desc.getEntityFilter();
        Map<String, Map<Integer, Integer>> cardIds = player.getStatistics().getCardsPlayed();
        for (String card : cardIds.keySet()) {
            for (int turn : cardIds.get(card).keySet()) {
                for (int i = 0; i < cardIds.get(card).get(turn); i++) {
                    playedCards.add(context.getCardById(card));
                }
            }
        }
        playedCards.shuffle();
        double i = 0;
        for (Card card : playedCards) {
            if (filter != null && !filter.matches(context, player, card)) {
                continue;
            }
            context.fireGameEvent(new CardRevealedEvent(context, player.getId(), card, i += 1.2));
            switch (card.getCardType()) {
                case SPELL:
                    SpellDesc spell = CastRandomSpellSpell.create(card.getCardId(), false);
                    SpellUtils.castChildSpell(context, player, spell, player, null);
                    break;
                case MINION:
                    MinionCard minionCard = (MinionCard) card;
                    context.getLogic().summon(player.getId(), minionCard.summon(), card, -1, false);
                    break;
                case WEAPON:
                    if (player.hasAttribute(Attribute.REPLACED_WEAPON_SLOT)) {
                        break;
                    }
                    WeaponCard weaponCard = (WeaponCard) card;
                    context.getLogic().equipWeapon(player.getId(), weaponCard.getWeapon(), false);
                    break;
                case REPLACE_HERO:
                    ReplaceHeroCard replaceHeroCard = (ReplaceHeroCard) card;
                    context.getLogic().replaceHero(player.getId(), replaceHeroCard, null);
                    break;
            }
        }


    }
}
