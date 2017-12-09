package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class KingsbaneShuffleSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        Weapon weapon = (Weapon) source;
        WeaponCard gonnaShuffle = (WeaponCard) context.getCardById(weapon.getSourceCard().getCardId());
        Attribute[] niceAttributes = {Attribute.ENCHANTMENT_ATTACK, Attribute.ENCHANTMENT_HP, Attribute.LIFESTEAL, Attribute.POISONOUS};
        for (Attribute attribute : niceAttributes) {
            if (weapon.hasAttribute(attribute)) {
                gonnaShuffle.setAttribute(attribute, weapon.getAttribute(attribute));
            }
        }
        gonnaShuffle.setAttribute(Attribute.ATTACK_BONUS, weapon.getAttributeValue(Attribute.ENCHANTMENT_ATTACK));
        gonnaShuffle.setAttribute(Attribute.HP_BONUS, weapon.getAttributeValue(Attribute.ENCHANTMENT_HP));
        context.getLogic().shuffleToDeck(player,gonnaShuffle);
    }
}
