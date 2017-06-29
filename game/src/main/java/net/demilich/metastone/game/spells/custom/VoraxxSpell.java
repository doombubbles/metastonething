package net.demilich.metastone.game.spells.custom;

import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.SummonCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class VoraxxSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		Summon plant = ((SummonCard) CardCatalogue.getCardById("token_plant")).summon();
		context.getLogic().summon(player.getId(), plant, null, boardPosition, false);


		Card card = SpellUtils.getCard(context, desc);
		if (card == null) {
			return;
		}
		if (card instanceof SpellCard) {
			SpellCard spell = (SpellCard) card;
			spell.setSpell(spell.getSpell().addArg(SpellArg.FILTER, null));
			SpellUtils.castChildSpell(context, player, spell.getSpell(), source, plant);
		}
		
	}

}
