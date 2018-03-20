package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.SummonCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.AddDeathrattleSpell;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class MoatLurkerSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Minion minion = (Minion) target;
		player = context.getPlayer(source.getOwner());
		TargetPlayer targetPlayer = desc.contains(SpellArg.TARGET_PLAYER) ? desc.getTargetPlayer() :
				minion.getOwner() == source.getOwner() ? TargetPlayer.SELF : TargetPlayer.OPPONENT;
		source.removeAttribute(Attribute.DEATHRATTLES);
		SpellDesc deathrattle = SummonSpell.create(targetPlayer, (SummonCard) minion.getSourceCard());
		SpellDesc addDeathrattleSpell = AddDeathrattleSpell.create(deathrattle);
		SpellDesc destroySpell = DestroySpell.create(target.getReference());
		SpellUtils.castChildSpell(context, player, destroySpell, source, target);
		for (int i = 1; i <= desc.getInt(SpellArg.HOW_MANY, 1); i++) {
			SpellUtils.castChildSpell(context, player, addDeathrattleSpell, source, source);
		}

	}

}
