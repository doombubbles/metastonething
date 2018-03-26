package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

public class TransformToRandomMinionSpell extends TransformMinionSpell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(TransformToRandomMinionSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList filteredMinions = new CardList();
		if (desc.contains(SpellArg.CARD_FILTER)) {
			EntityFilter filter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);

			CardList allMinions = CardCatalogue.query(context.getDeckFormat(), CardType.MINION);
			for (Card card : allMinions) {
				MinionCard minionCard = (MinionCard) card;
				if (filter == null || filter.matches(context, player, card)) {
					filteredMinions.add(minionCard);
				}
			}

			if (filteredMinions.getCount() > 1) {
				//Don't even ask me why I have to do this
				filteredMinions.removeAll(card -> card.getName().equals("Snowfury Giant"));
				filteredMinions.removeAll(card -> card.getCardId().equalsIgnoreCase(context.getCardById("minion_snowfury_giant").getCardId()));
				filteredMinions.removeAll(card -> card.getName().equals("The Darkness"));
			}
		}


		for (Card card : SpellUtils.getCards(context, desc)) {
			if (card instanceof MinionCard) {
				filteredMinions.add(card);
			}
		}

		MinionCard randomCard = (MinionCard) filteredMinions.getRandom();

		if (randomCard != null) {
			SpellDesc transformMinionSpell = TransformMinionSpell.create(randomCard.getCardId());
			super.onCast(context, player, transformMinionSpell, source, target);
		}
	}

}
