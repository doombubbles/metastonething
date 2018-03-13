package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.CardCatalogue;

public class ExploreUngoroSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList newDeck = new CardList();
		for (Card card : player.getDeck()) {
			newDeck.add(CardCatalogue.getCardById("spell_choose_a_path"));
		}
		player.getDeck().removeAll();
		player.getDeck().addAll(newDeck);
		
	}
	
}
