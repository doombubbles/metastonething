package net.demilich.metastone.game.spells.custom;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class CuriousSpell extends Spell {
	
	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CuriousSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);  
		return new SpellDesc(arguments);
	}
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = new CardList();
		Player opponent = context.getOpponent(player);
		CardList potentialCards = CardCatalogue.query(context.getDeckFormat());
		CardList deck = opponent.getStartingDeck();
		deck.removeAll(p -> p.getHeroClass() == HeroClass.ANY);
		if (deck.isEmpty()) {
			deck = context.getOpponent(player).getStartingDeck();
			potentialCards.removeAll(p -> (p.getHeroClass() != opponent.getHero().getHeroClass() || p.getHeroClass() != opponent.getHero().getHeroClass()));
		} else potentialCards.removeAll(p -> p.getHeroClass() != opponent.getHero().getHeroClass());
		potentialCards.shuffle();
		potentialCards.removeAll(deck::contains);
		Card realCard = deck.getRandom();
		Card wrongCard1 = potentialCards.get(0);
		Card wrongCard2 = potentialCards.get(1);
		cards.add(realCard);
		cards.add(wrongCard1);
		cards.add(wrongCard2);
		cards.shuffle();

		if (SpellUtils.getDiscover(context, player, desc, cards).getCard() == realCard) {
			context.getLogic().receiveCard(player.getId(), context.getCardById(realCard.getCardId()).clone());
		}
	}

}
