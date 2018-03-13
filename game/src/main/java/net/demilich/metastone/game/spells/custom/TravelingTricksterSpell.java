package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.ReceiveCardSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Arrays;
import java.util.List;

public class TravelingTricksterSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        List<HeroClass> secretClasses = Arrays.asList(HeroClass.PALADIN, HeroClass.MAGE, HeroClass.HUNTER, HeroClass.ROGUE);
        CardList cards = CardCatalogue.query(context.getDeckFormat());
        CardList cards1 = new CardList();
        cards.shuffle();
        for (Card card : cards) {
            if (cards1.getCount() >= 3) {
                break;
            }
            if (card.hasAttribute(Attribute.SECRET)) {
                if (secretClasses.contains(player.getHero().getHeroClass())) {
                    if (card.getHeroClass().equals(player.getHero().getHeroClass())) {
                        cards1.add(card);
                    }
                } else {
                    if (card.getHeroClass().equals(HeroClass.HUNTER)) {
                        cards1.add(card);
                    }
                }
            }
        }
        SpellUtils.getDiscover(context, player, ReceiveCardSpell.create(player), cards1);
        double i = 1.6;
        for (Card card : cards1) {
            context.fireGameEvent(new CardRevealedEvent(context, context.getOpponent(player).getId(), card, i++));
        }
    }
}
