package net.demilich.metastone.game.spells.custom;

import java.util.Map.Entry;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class FinleyRogueSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		
		CardCollection cards = new CardCollection();
		for (Entry<String, Integer> minion : player.roguequest.entrySet()) {
			for (int i = 1; i < minion.getValue(); i++) {
				cards.add(CardCatalogue.getCardByName(minion.getKey()));
			}
		}
		CardCollection cards2 = new CardCollection();
		cards.shuffle();
		if (cards.toList().size() > 0) {
			cards2.add(cards.get(0));
		}	
		if (cards.toList().size() > 1) {
			cards2.add(cards.get(1));
		}
		if (cards.toList().size() > 2) {
			cards2.add(cards.get(2));
		}
		
		Card chosencard = SpellUtils.getDiscover(context, player, desc, cards2).getCard();
		
		context.getLogic().receiveCard(player.getId(), chosencard);
		
		
	}

}
