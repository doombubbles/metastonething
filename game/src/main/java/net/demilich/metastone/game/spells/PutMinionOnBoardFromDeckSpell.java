package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class PutMinionOnBoardFromDeckSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		boolean exlcusive = desc.contains(SpellArg.EXCLUSIVE);
		boolean summonSuccess = false;
		if (target == null) {
			return;
		}
		Card card = (Card) target;
		if (!player.getDeck().contains(card)) {
			return;
		}
		if (card.getCardType().equals(CardType.MINION)) {
			MinionCard minionCard = (MinionCard) card;
			player.getDeck().remove(minionCard);
			player.getSetAsideZone().add(minionCard);

			summonSuccess = context.getLogic().summon(player.getId(), minionCard.summon());

			// re-add the card here if we removed it before
			player.getSetAsideZone().remove(minionCard);
			player.getDeck().add(minionCard);
		} else if (exlcusive) {
			context.fireGameEvent(new CardRevealedEvent(context, player.getId(), card, 0));
		}
		if (summonSuccess || exlcusive) {
			context.getLogic().removeCardFromDeck(player.getId(), card);
		}
	}

}
