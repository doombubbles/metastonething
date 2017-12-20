package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

public class SummonAndDoSomethingSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		MinionCard minionCard = null;
		if (desc.contains(SpellArg.CARD)) {
			minionCard = (MinionCard) SpellUtils.getCard(context, desc);
		} else {
			CardFilter cardFilter = (CardFilter) desc.get(SpellArg.CARD_FILTER);
			CardCollection cards = CardCatalogue.query(context.getDeckFormat());
			cards.shuffle();
			for (Card card : cards) {
				if (cardFilter.matches(context, player, card)) {
					minionCard = (MinionCard) context.getCardById(card.getCardId());
				}
			}
		}
		Minion targetMinion = minionCard.summon();
		context.getLogic().summon(player.getId(), targetMinion);
		SpellUtils.castChildSpell(context, player, spell, targetMinion, target);


	}

}
