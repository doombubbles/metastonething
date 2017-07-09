package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class ReviveMinionSpell extends Spell {

	public static SpellDesc create(EntityReference target) {
		return create(target, 0, 0);
	}

	public static SpellDesc create(EntityReference target, int hpAdjustment, int attackAdjustment) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReviveMinionSpell.class);
		arguments.put(SpellArg.HP_BONUS, hpAdjustment);
		arguments.put(SpellArg.ATTACK_BONUS, attackAdjustment);
		arguments.put(SpellArg.TARGET, target);

		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int hpAdjustment = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		int attackAdjustment = desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0);
		Actor targetActor = (Actor) target;
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		MinionCard minionCard = (MinionCard) targetActor.getSourceCard();
		Minion minion = minionCard.summon();
		if (hpAdjustment < 0) {
			minion.setHp(hpAdjustment * -1);
		}
		if (hpAdjustment > 0) {
			minion.modifyHpBonus(hpAdjustment);
		}
		if (attackAdjustment > 0) {
			minion.modifyAttribute(Attribute.ATTACK_BONUS, attackAdjustment);
		}
		context.getLogic().summon(player.getId(), minion, null, boardPosition, false);
	}

}
