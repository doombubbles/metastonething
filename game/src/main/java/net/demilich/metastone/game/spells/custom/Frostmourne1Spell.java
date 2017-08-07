package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SummonCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.AddDeathrattleSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class Frostmourne1Spell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		 Card minion = ((Minion) context.resolveSingleTarget(context.getEventTargetStack().peek())).getSourceCard();
		 SpellDesc spell = AddDeathrattleSpell.create(EntityReference.FRIENDLY_WEAPON, SummonSpell.create((SummonCard) minion));
		 SpellUtils.castChildSpell(context, player, spell, source, player.getHero().getWeapon());
	}

}
