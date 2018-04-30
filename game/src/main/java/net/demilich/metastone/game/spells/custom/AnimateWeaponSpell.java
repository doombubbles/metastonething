package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.EquipWeaponSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class AnimateWeaponSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        Weapon weapon;
        try {
            weapon = (Weapon) target;
        } catch (Exception e) {
            context.logError("Not a weapon to animate!");
            return;
        }
        MinionCardDesc minionCardDesc = new MinionCardDesc();
        minionCardDesc.id = context.getLogic().generateCardID();
        minionCardDesc.name = weapon.getName();
        minionCardDesc.baseAttack = weapon.getAttack();
        minionCardDesc.baseHp = weapon.getDurability();
        minionCardDesc.heroClass = weapon.getSourceCard().getHeroClass();
        minionCardDesc.type = CardType.MINION;
        minionCardDesc.rarity = Rarity.FREE;
        minionCardDesc.set = CardSet.BASIC;
        minionCardDesc.collectible = false;
        minionCardDesc.baseManaCost = weapon.getSourceCard().getBaseManaCost();
        minionCardDesc.description = "Deathrattle: Re-equip this as a weapon.";
        MinionCard newCard = (MinionCard) minionCardDesc.createInstance();
        newCard.addDeathrattle(EquipWeaponSpell.create(weapon.getSourceCard().getCardId()));
        context.addTempCard(newCard);
        context.getLogic().summon(player.getId(), newCard.summon(), newCard, -1, false);
        context.getLogic().removeAttribute(player.getHero().getWeapon(), Attribute.DEATHRATTLES);
        context.getLogic().removeSpellTriggers(player.getHero().getWeapon(), false);
        context.getLogic().destroy((Actor) weapon);
    }
}
