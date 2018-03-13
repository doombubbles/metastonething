package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;

public class SummonAndDoSomethingSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		Minion minion = null;
		if (desc.contains(SpellArg.CARD)) {
			minion = ((MinionCard) SpellUtils.getCard(context, desc)).summon();
			context.getLogic().summon(player.getId(), minion);
		} else if (desc.contains(SpellArg.CARD_FILTER)){
			CardFilter cardFilter = (CardFilter) desc.get(SpellArg.CARD_FILTER);
			CardList cards = CardCatalogue.query(context.getDeckFormat());
			cards.shuffle();
			for (Card card : cards) {
				if (cardFilter.matches(context, player, card)) {
					minion = ((MinionCard) context.getCardById(card.getCardId())).summon();
				}
			}
			context.getLogic().summon(player.getId(), minion);
		} else {
			Minion clone = ((Minion) target).clone();
			clone.clearSpellTriggers();
			context.getLogic().summon(player.getId(), clone);
			for (IGameEventListener trigger : context.getTriggersAssociatedWith(target.getReference())) {
				IGameEventListener triggerClone = trigger.clone();
				context.getLogic().addGameEventListener(player, triggerClone, clone);
			}
			minion = clone;
		}
		SpellUtils.castChildSpell(context, player, spell, minion, target);


	}

}
