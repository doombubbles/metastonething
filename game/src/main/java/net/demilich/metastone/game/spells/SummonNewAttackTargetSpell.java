package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.targeting.EntityReference;

public class SummonNewAttackTargetSpell extends Spell {

	public static SpellDesc create(MinionCard minionCard) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonNewAttackTargetSpell.class);
		arguments.put(SpellArg.CARD, minionCard);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
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
		if (targetMinion.getOwner() > -1) {
			context.getEnvironment().put(Environment.TARGET_OVERRIDE, targetMinion.getReference());
		}

	}

}
