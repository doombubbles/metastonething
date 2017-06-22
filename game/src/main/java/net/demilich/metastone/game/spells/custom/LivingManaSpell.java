package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.CardCatalogue;


public class LivingManaSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int number_of_minions = player.getMinions().size();
		int trees_to_summon = Math.min((7 - number_of_minions), player.getMaxMana());
		for (int i = 0; i < trees_to_summon; i++) {
			MinionCard minion = (MinionCard) CardCatalogue.getCardById("token_mana_treant");
			context.getLogic().summon(player.getId(), minion.summon(), null, SpellUtils.getBoardPosition(context, player, desc, source), false);
			player.setMaxMana(player.getMaxMana() - 1);
		}
		if (player.getMana() > player.getMaxMana()) {
			player.setMana(player.getMaxMana());
		}
		
		
	}
	
}
