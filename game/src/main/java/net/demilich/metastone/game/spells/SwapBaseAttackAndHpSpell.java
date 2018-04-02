package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

public class SwapBaseAttackAndHpSpell extends Spell {

	public static SpellDesc create() {
		return create(null);
	}

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SwapBaseAttackAndHpSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int attack = target.getAttributeValue(Attribute.ATTACK);
		int hp = target.getAttributeValue(Attribute.HP);
		int baseAttack = target.getAttributeValue(Attribute.BASE_ATTACK);
		int baseHp = target.getAttributeValue(Attribute.BASE_HP);
		int attackBuff = target.getAttributeValue(Attribute.ATTACK_BONUS);
		int hpBuff = target.getAttributeValue(Attribute.HP_BONUS);
		target.setAttribute(Attribute.HP, attack);
		target.setAttribute(Attribute.ATTACK, hp);
		target.setAttribute(Attribute.BASE_HP, baseAttack);
		target.setAttribute(Attribute.BASE_ATTACK, baseHp);
		target.setAttribute(Attribute.ATTACK_BONUS, hpBuff);
		target.setAttribute(Attribute.HP_BONUS, attackBuff);
	}

}