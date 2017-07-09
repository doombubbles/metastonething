package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;

public class SummonSpecialSpell extends Spell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonOneOneCopySpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		String name = (String) desc.get(SpellArg.NAME); 
		Race race = Race.valueOf(desc.getString(SpellArg.SECONDARY_NAME));
		int attack = desc.getInt(SpellArg.ATTACK_BONUS);
		int hp = desc.getInt(SpellArg.HP_BONUS);
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		MinionCard minionCard = null;
		if (cardSource != null || cardFilter != null) {
			CardCollection relevantMinions = null;
			if (cardSource != null) {
				CardCollection allCards = cardSource.getCards(context, player);
				relevantMinions = new CardCollection();
				for (Card card : allCards) {
					if (card.getCardType().isCardType(CardType.MINION) && (cardFilter == null || cardFilter.matches(context, player, card))) {
						relevantMinions.add(card);
					}
				}
			} else {
				CardCollection allMinions = CardCatalogue.query(context.getDeckFormat(), CardType.MINION);
				relevantMinions = new CardCollection();
				for (Card card : allMinions) {
					if (cardFilter.matches(context, player, card)) {
						relevantMinions.add(card);
					}
				}
			}
			minionCard = (MinionCard) relevantMinions.getRandom();
		} else {
			minionCard = (MinionCard) ((Minion) target).getSourceCard();
		}

		if (minionCard != null) {
			MinionCard newMinion = (MinionCard) minionCard.clone();
			newMinion.setName(name);
			newMinion.setRace(race);
			Minion minion = newMinion.summon();
			if (context.getLogic().summon(player.getId(), minion, null, boardPosition, false)) {
				minion.setAttack(1 + attack);
				minion.setHp(1 + hp);
				minion.setMaxHp(1 + hp);
				
			}
		}
	}

}
