package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.SpellCardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.*;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.valueprovider.*;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CombineSpellsSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        desc = desc.addArg(SpellArg.SPELL, NullSpell.create("Combine:"));
        
        HeroClass heroClass = player.getHero().getHeroClass();
        EntityFilter filter = desc.getEntityFilter();

        CardList spellList1 = CardCatalogue.query(context.getDeckFormat(), CardType.SPELL, heroClass);
        CardList spellList2 = CardCatalogue.query(context.getDeckFormat(), CardType.SPELL, heroClass);
        if (filter != null) {
            spellList1.removeAll(card -> !filter.matches(context, player, card));
            spellList2.removeAll(card -> !filter.matches(context, player, card));
        }
        spellList1.removeAll(card -> card.hasAttribute(Attribute.SECRET) || card.hasAttribute(Attribute.QUEST) || card instanceof ChooseOneCard);
        spellList2.removeAll(card -> card.hasAttribute(Attribute.SECRET) || card.hasAttribute(Attribute.QUEST) || card instanceof ChooseOneCard);

        int spellsCosting0 = 0;
        for (Card card : spellList1) {
            if (card.getBaseManaCost() == 0 && !card.getName().contains("Forbidden")) {
                spellsCosting0++;
            }
        }
        if (spellsCosting0 < 1) {
            spellList1.removeAll(card -> card.getBaseManaCost() > 9);
        }

        CardList discoverSpells = new CardList();
        for (int i = 1; i <= 3; i++) {
            Card card = spellList1.getRandom();
            discoverSpells.add(card);
            spellList1.remove(card);
        }

        SpellCard firstChosenSpell = (SpellCard) SpellUtils.getDiscover(context, player, desc, discoverSpells).getCard();
        int firstManaCost = firstChosenSpell.getBaseManaCost();
        TargetSelection firstTargetSelection = firstChosenSpell.getTargetRequirement();

        spellList2.remove(firstChosenSpell);
        spellList2.removeAll(card -> firstManaCost + card.getBaseManaCost() > 10);
        spellList2.removeAll(card -> !evaluateTargetSelection(firstTargetSelection, ((SpellCard) card).getTargetRequirement()));

        discoverSpells = new CardList();
        for (int i = 1; i <= 3; i++) {
            Card card = spellList2.getRandom();
            discoverSpells.add(card);
            spellList2.remove(card);
        }

        desc = desc.addArg(SpellArg.SPELL, NullSpell.create("Combine:"));
        SpellCard secondChosenSpell = (SpellCard) SpellUtils.getDiscover(context, player, desc, discoverSpells).getCard();
        int secondManaCost = secondChosenSpell.getBaseManaCost();
        TargetSelection secondTargetSelection = secondChosenSpell.getTargetRequirement();

        SpellCardDesc spellCardDesc = new SpellCardDesc();
        spellCardDesc.id = context.getLogic().generateCardID();
        spellCardDesc.name = desc.getString(SpellArg.NAME);
        spellCardDesc.baseManaCost = firstManaCost + secondManaCost;
        spellCardDesc.description = "Combination of '" + firstChosenSpell.getName() + "' and '" + secondChosenSpell.getName() + "'.";
        spellCardDesc.heroClass = heroClass;
        spellCardDesc.rarity = Rarity.FREE;
        spellCardDesc.type = CardType.SPELL;
        spellCardDesc.targetSelection = combineTargetSelection(firstTargetSelection, secondTargetSelection);
        spellCardDesc.spell = MetaSpell.create(firstChosenSpell.getSpell(), secondChosenSpell.getSpell());
        if (firstChosenSpell.getCondition() != null || secondChosenSpell.getCondition() != null) {
            Map<ConditionArg, Object> arguments = ConditionDesc.build(AndCondition.class);
            if (firstChosenSpell.getCondition() != null && secondChosenSpell.getCondition() != null) {
                Condition[] conditions = new Condition[2];
                conditions[0] = firstChosenSpell.getCondition();
                conditions[1] = secondChosenSpell.getCondition();
                arguments.put(ConditionArg.CONDITIONS, conditions);
            } else if (firstChosenSpell.getCondition() != null) {
                arguments.put(ConditionArg.CONDITIONS, new Condition[]{firstChosenSpell.getCondition()});
            } else if (secondChosenSpell.getCondition() != null) {
                arguments.put(ConditionArg.CONDITIONS, new Condition[]{secondChosenSpell.getCondition()});
            }
            spellCardDesc.condition = new ConditionDesc(arguments);
        }
        if (firstChosenSpell.getGlow() != null || secondChosenSpell.getGlow() != null) {
            if (firstChosenSpell.getGlow() != null && secondChosenSpell.getGlow() != null) {
                Map<ConditionArg, Object> arguments = ConditionDesc.build(OrCondition.class);
                Condition[] conditions = new Condition[2];
                conditions[0] = firstChosenSpell.getGlow().create();
                conditions[1] = secondChosenSpell.getGlow().create();
                arguments.put(ConditionArg.CONDITIONS, conditions);
                spellCardDesc.glow = new ConditionDesc(arguments);
            } else if (firstChosenSpell.getGlow() != null) {
                spellCardDesc.glow = firstChosenSpell.getGlow();
            } else if (secondChosenSpell.getGlow() != null) {
                spellCardDesc.glow = secondChosenSpell.getGlow();
            }
        }
        Arrays.asList(Attribute.LIFESTEAL, Attribute.ECHO).forEach(attribute -> {
            if (firstChosenSpell.hasAttribute(attribute) || secondChosenSpell.hasAttribute(attribute)) {
                spellCardDesc.attributes.put(attribute, true);
            }
        });
        spellCardDesc.collectible = false;
        if (firstChosenSpell.getManaCostModifier() != null || secondChosenSpell.getManaCostModifier() != null) {
            Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(AlgebraicValueProvider.class);
            if (firstChosenSpell.getManaCostModifier() != null) {
                arguments.put(ValueProviderArg.VALUE_1, firstChosenSpell.getManaCostModifier());
            } else arguments.put(ValueProviderArg.VALUE_1, 0);
            if (secondChosenSpell.getManaCostModifier() != null) {
                arguments.put(ValueProviderArg.VALUE_2, secondChosenSpell.getManaCostModifier());
            } else arguments.put(ValueProviderArg.VALUE_2, 0);
            arguments.put(ValueProviderArg.OPERATION, AlgebraicOperation.ADD);

            spellCardDesc.manaCostModifier = new ValueProviderDesc(arguments);
        }
        spellCardDesc.set = CardSet.CUSTOM;

        Card card = spellCardDesc.createInstance();
        context.getLogic().receiveCard(player.getId(), card);
        context.addTempCard(card);
    }

    private boolean evaluateTargetSelection(TargetSelection targetSelection1, TargetSelection targetSelection2) {
        switch (targetSelection1) {
            case HEROES:
                return targetSelection2.equals(TargetSelection.ANY) || targetSelection2.equals(TargetSelection.NONE);
            case ENEMY_CHARACTERS:
                return !targetSelection2.equals(TargetSelection.FRIENDLY_MINIONS) && !targetSelection2.equals(TargetSelection.FRIENDLY_CHARACTERS);
            case ENEMY_MINIONS:
                return !targetSelection2.equals(TargetSelection.FRIENDLY_MINIONS) && !targetSelection2.equals(TargetSelection.FRIENDLY_CHARACTERS);
            case FRIENDLY_CHARACTERS:
                return !targetSelection2.equals(TargetSelection.ENEMY_MINIONS) && !targetSelection2.equals(TargetSelection.ENEMY_CHARACTERS);
            case FRIENDLY_MINIONS:
                return !targetSelection2.equals(TargetSelection.ENEMY_MINIONS) && !targetSelection2.equals(TargetSelection.ENEMY_CHARACTERS);
            case MINIONS:
                return !targetSelection2.equals(TargetSelection.HEROES);
        }
        return true;
    }

    private TargetSelection combineTargetSelection(TargetSelection targetSelection1, TargetSelection targetSelection2) {
        if (targetSelection1.equals(targetSelection2)) {
            return targetSelection1;
        } else if (targetSelection1.equals(TargetSelection.NONE)) {
            return targetSelection2;
        } else if (targetSelection2.equals(TargetSelection.NONE)) {
            return targetSelection1;
        }else if ((targetSelection1.equals(TargetSelection.MINIONS) && targetSelection2.equals(TargetSelection.ANY))
                || (targetSelection2.equals(TargetSelection.MINIONS) && targetSelection1.equals(TargetSelection.ANY))) {
            return TargetSelection.MINIONS;
        } else if ((targetSelection1.equals(TargetSelection.FRIENDLY_MINIONS) && (Arrays.asList(TargetSelection.ANY, TargetSelection.FRIENDLY_CHARACTERS, TargetSelection.MINIONS).contains(targetSelection2)))
                || (targetSelection2.equals(TargetSelection.FRIENDLY_MINIONS) && (Arrays.asList(TargetSelection.ANY, TargetSelection.FRIENDLY_CHARACTERS, TargetSelection.MINIONS).contains(targetSelection1)))) {
            return TargetSelection.FRIENDLY_MINIONS;
        } else if ((targetSelection1.equals(TargetSelection.FRIENDLY_CHARACTERS) && (Arrays.asList(TargetSelection.ANY, TargetSelection.FRIENDLY_CHARACTERS).contains(targetSelection2)))
                || (targetSelection2.equals(TargetSelection.FRIENDLY_CHARACTERS) && (Arrays.asList(TargetSelection.ANY, TargetSelection.FRIENDLY_CHARACTERS).contains(targetSelection1)))) {
            return TargetSelection.FRIENDLY_CHARACTERS;
        } else if ((targetSelection1.equals(TargetSelection.ENEMY_MINIONS) && (Arrays.asList(TargetSelection.ANY, TargetSelection.ENEMY_CHARACTERS, TargetSelection.MINIONS).contains(targetSelection2)))
                || (targetSelection2.equals(TargetSelection.ENEMY_MINIONS) && (Arrays.asList(TargetSelection.ANY, TargetSelection.ENEMY_CHARACTERS, TargetSelection.MINIONS).contains(targetSelection1)))) {
            return TargetSelection.ENEMY_MINIONS;
        } else if ((targetSelection1.equals(TargetSelection.ENEMY_CHARACTERS) && (Arrays.asList(TargetSelection.ANY, TargetSelection.ENEMY_CHARACTERS).contains(targetSelection2)))
                || (targetSelection2.equals(TargetSelection.ENEMY_CHARACTERS) && (Arrays.asList(TargetSelection.ANY, TargetSelection.ENEMY_CHARACTERS).contains(targetSelection1)))) {
            return TargetSelection.ENEMY_CHARACTERS;
        } else if (targetSelection1.equals(TargetSelection.ANY) && targetSelection2.equals(TargetSelection.ANY)) {
            return TargetSelection.ANY;
        }
        return TargetSelection.ANY;
    }
}
