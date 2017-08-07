package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class ArmyoftheDeadSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		for (int i = 0; i < 5; i++) {
			Card card = player.getDeck().get(0);
			context.fireGameEvent(new CardRevealedEvent(context, player.getId(), card, 1.2 + 1.2 * i));
			if (card.getCardType().equals(CardType.MINION)) {
				SpellDesc summonSpell = SummonSpell.create((MinionCard) card);
				SpellUtils.castChildSpell(context, player, summonSpell, source, target);
			}
			player.getDeck().removeFirst();
		}
		
	}

}
