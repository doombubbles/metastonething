package net.demilich.metastone.game.spells.custom;

import java.util.Arrays;
import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
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
		
		CardCollection cards = new CardCollection();
		CardCollection potentialCards = new CardCollection();
		CardCollection add = new CardCollection();
		CardCollection remove = new CardCollection();
		potentialCards = CardCatalogue.query(context.getDeckFormat());
		for (Card card : potentialCards) {
			if (card.getHeroClass() != context.getOpponent(player).getHero().getHeroClass() && card.getHeroClass() != HeroClass.ANY) {
				remove.add(card);
			} else if (card.getHeroClass() == context.getOpponent(player).getHero().getHeroClass()) {
				add.add(card);
				add.add(card);
				add.add(card);
			}
		}
		potentialCards.removeAll(p -> remove.contains(p));
		potentialCards.addAll(add);
		
		Player opponent = context.getOpponent(player);
		Card random1 = opponent.getDeck().getRandom();
		Card random2 = potentialCards.getRandom();
		Card random3 = potentialCards.getRandom();
		
		double i = Math.random();
		if (i < 0.33) {
			cards.add(random1);
			cards.add(random2);
			cards.add(random3);
		} else if (i > .66) {
			cards.add(random2);
			cards.add(random1);
			cards.add(random3);
		} else {
			cards.add(random2);
			cards.add(random3);
			cards.add(random1);
		}

		
		
		Card chosenCard = SpellUtils.getDiscover(context, player, desc, cards).getCard();
		
		if (chosenCard == random1) {
			context.getLogic().receiveCard(player.getId(), chosenCard);
		}
		
		
		
		return;
	}

}
