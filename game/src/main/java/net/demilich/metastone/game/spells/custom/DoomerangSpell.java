package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class DoomerangSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int damage = context.getLogic().damage(player, (Actor) target, player.getHero().getWeapon().getAttributeValue(Attribute.ATTACK), source, true);
		if (player.getHero().getWeapon().hasAttribute(Attribute.LIFESTEAL)) {
			context.getLogic().heal(player, player.getHero(), damage, source);
		}
		if (damage > 0 && player.getHero().getWeapon().hasAttribute(Attribute.POISONOUS)) {
			context.getLogic().destroy((Actor) target); 
		}
		Weapon weapon = player.getHero().getWeapon();
		if (weapon.getSourceCard().getCardId().equals("weapon_kingsbane")) {

		} else {

			context.getLogic().receiveCard(player.getId(), context.getCardById(weapon.getSourceCard().getCardId()));
		}

		context.getLogic().removeSpellTriggers(weapon, false);
		context.getLogic().destroy((Actor) weapon);
	}

}
