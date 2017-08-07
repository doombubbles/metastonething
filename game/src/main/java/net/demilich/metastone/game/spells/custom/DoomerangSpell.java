package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class DoomerangSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int damage = context.getLogic().damage(player, (Actor) target, player.getHero().getWeapon().getAttributeValue(Attribute.ATTACK), source, false);
		if (player.getHero().getWeapon().hasAttribute(Attribute.LIFESTEAL)) {
			context.getLogic().heal(player, player.getHero(), damage, source);
		}
		if (damage > 0 && player.getHero().getWeapon().hasAttribute(Attribute.POISONOUS)) {
			context.getLogic().destroy((Actor) target); 
		}
		context.getLogic().removeSpellTriggers(player.getHero().getWeapon(), false);
		context.getLogic().receiveCard(player.getId(), context.getCardById(player.getHero().getWeapon().getSourceCard().getCardId()));
		context.getLogic().destroy((Actor) player.getHero().getWeapon());
	}

}
