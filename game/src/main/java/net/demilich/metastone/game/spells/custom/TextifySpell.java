package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextifySpell extends Spell {

    List<Attribute> badAttibutes = new ArrayList<>(Arrays.asList(Attribute.BASE_ATTACK, Attribute.BASE_HP, Attribute.BASE_MANA_COST, Attribute.ATTACK, Attribute.HP, Attribute.RACE));

    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardCollection baseMinions = CardCatalogue.query(context.getDeckFormat(), CardType.MINION);
        baseMinions.removeAll(card1 -> card1.hasAttribute(Attribute.CHOOSE_ONE));
        baseMinions.removeAll(card -> card.getDescription().equals(""));
        CardCollection deckCards = new CardCollection();
        CardCollection handCards = new CardCollection();
        for (Card card : player.getDeck()) {
            if (card.getCardType().equals(CardType.MINION)) {
                MinionCard minionCard = (MinionCard) card;
                deckCards.add(minionCard);
            }
        }
        for (Card card : player.getHand()) {
            if (card.getCardType().equals(CardType.MINION)) {
                MinionCard minionCard = (MinionCard) card;
                handCards.add(minionCard);
            }
        }
        for (Card card : deckCards) {
            MinionCard minionCard = (MinionCard) card;
            MinionCard ogCard = (MinionCard) minionCard.clone();
            MinionCardDesc minionCardDesc = ((MinionCard)baseMinions.getRandom()).getDesc();
            minionCardDesc.name = ogCard.getName();
            minionCardDesc.race = ogCard.getRace();
            minionCardDesc.heroClass = ogCard.getHeroClass();
            minionCardDesc.rarity = ogCard.getRarity();
            minionCardDesc.baseAttack = ogCard.getBaseAttack();
            minionCardDesc.baseHp = ogCard.getBaseHp();
            minionCardDesc.baseManaCost = ogCard.getBaseManaCost();
            MinionCard randomCard = new MinionCard(minionCardDesc);
            randomCard.setOwner(minionCard.getOwner());
            randomCard.setLocation(ogCard.getLocation());
            randomCard.setId(ogCard.getId());
            player.getDeck().replace(minionCard, randomCard);
        }
        for (Card card : handCards) {
            MinionCard minionCard = (MinionCard) card;
            MinionCard ogCard = (MinionCard) minionCard.clone();
            MinionCardDesc minionCardDesc = ((MinionCard)baseMinions.getRandom()).getDesc();
            minionCardDesc.name = ogCard.getName();
            minionCardDesc.race = ogCard.getRace();
            minionCardDesc.heroClass = ogCard.getHeroClass();
            minionCardDesc.rarity = ogCard.getRarity();
            minionCardDesc.baseAttack = ogCard.getBaseAttack();
            minionCardDesc.baseHp = ogCard.getBaseHp();
            minionCardDesc.baseManaCost = ogCard.getBaseManaCost();
            MinionCard randomCard = new MinionCard(minionCardDesc);
            randomCard.setOwner(minionCard.getOwner());
            randomCard.setLocation(ogCard.getLocation());
            randomCard.setId(ogCard.getId());
            player.getHand().replace(minionCard, randomCard);
        }
    }
}
