package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.events.HeroPowerChangedEvent;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.FilterArg;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.heroes.powers.HeroPower;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChangeHeroPowerSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(ChangeHeroPowerSpell.class);

	protected void changeHeroPower(GameContext context, String newHeroPower, Hero hero) {
		HeroPower heroPower = (HeroPower) context.getCardById(newHeroPower);
		logger.debug("{}'s hero power was changed to {}", hero.getName(), heroPower);
		context.removeTriggersAssociatedWith(hero.getHeroPower().getReference(), true);
		hero.setHeroPower(heroPower);
		context.fireGameEvent(new HeroPowerChangedEvent(context, hero.getOwner(), heroPower));
	}
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String heroPowerName = "";
		if (desc.contains(SpellArg.CARD)) {
			 heroPowerName = (String) desc.get(SpellArg.CARD);
		} else {
			List<String> potentialIds = new ArrayList<>();
			if (desc.contains(SpellArg.CARDS)) {
				String[] cards = (String[]) desc.get(SpellArg.CARDS);
				potentialIds = Arrays.asList(cards);
			} else {
				CardList heroPowers = CardCatalogue.getHeroPowers(context.getDeckFormat());;
				if (desc.contains(SpellArg.CARD_SOURCE)) {
					CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
					heroPowers = cardSource.getCards(context, player);
					heroPowers.removeAll(card -> !card.getCardType().equals(CardType.HERO_POWER));
				}
				EntityFilter filter = desc.getEntityFilter();
				for (Card heroPower : heroPowers) {
					if (filter != null) {
						if (filter.matches(context, player, heroPower)) {
							potentialIds.add(heroPower.getCardId());
						}
					} else potentialIds.add(heroPower.getCardId());
				}
			}
			heroPowerName = SpellUtils.getRandomTarget(potentialIds);
		}


		changeHeroPower(context, heroPowerName, player.getHero());
	}
}
