package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.minions.Rift;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.targeting.TargetSelection;

public class CardFilter extends EntityFilter {

	public CardFilter(FilterDesc desc) {
		super(desc);
	}

	private boolean heroClassTest(GameContext context, Player player, Card card, HeroClass heroClass) {
		if (heroClass == HeroClass.OPPONENT) {
			heroClass = context.getOpponent(player).getHero().getHeroClass();
		} else if (heroClass == HeroClass.SELF) {
			heroClass = player.getHero().getHeroClass();
		}
		
		if (heroClass != null && card.hasHeroClass(heroClass)) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity source) {
		Card card = null;
		if (entity instanceof Card) {
			card = (Card) entity;
		} else if (entity instanceof Actor) {
			Actor actor = (Actor) entity;
			card = actor.getSourceCard();
		} else {
			return false;
		}

		CardType cardType = (CardType) desc.get(FilterArg.CARD_TYPE);
		if (cardType != null && !card.getCardType().isCardType(cardType)) {
			return false;
		}
		Race race = (Race) desc.get(FilterArg.RACE);
		if (race != null && (card.getAttribute(Attribute.RACE) == Race.ALL)) {
			return true;
		} else if (race != null && race != card.getAttribute(Attribute.RACE)) {
			return false;
		}
		
		HeroClass[] heroClasses = (HeroClass[]) desc.get(FilterArg.HERO_CLASSES);
		if (heroClasses != null && heroClasses.length > 0) {
			boolean test = false;
			for (HeroClass heroClass : heroClasses) {
				test |= !heroClassTest(context, player, card, heroClass);
			}
			if (!test) {
				return false;
			}
		}
		
		HeroClass heroClass = (HeroClass) desc.get(FilterArg.HERO_CLASS);
		if (heroClass != null && heroClassTest(context, player, card, heroClass)) {
			return false;
		}
		
		if (desc.contains(FilterArg.MANA_COST)) {
			int manaCost = desc.getValue(FilterArg.MANA_COST, context, player, source, source, 0);
			if (manaCost != card.getBaseManaCost()) {
				return false;
			}
		}
		Rarity rarity = (Rarity) desc.get(FilterArg.RARITY);
		if (rarity != null && !card.getRarity().isRarity(rarity)) {
			return false;
		}
		
		if (card.hasAttribute(Attribute.QUEST)) {
			return false;
		}
		
		if (desc.contains(FilterArg.ATTRIBUTE) && desc.contains(FilterArg.OPERATION)) {
			Attribute attribute = (Attribute) desc.get(FilterArg.ATTRIBUTE);
			ComparisonOperation operation = (ComparisonOperation) desc.get(FilterArg.OPERATION);
			if (operation == ComparisonOperation.HAS || operation == null) {
				return card.hasAttribute(attribute);
			}
	
			int targetValue = desc.getValue(FilterArg.VALUE, context, player, entity, source, 0);
			int actualValue = card.getAttributeValue(attribute);
	
			return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
		}
		
		if (desc.contains(FilterArg.CARD_ID)) {
			String requiredCardId = desc.getString(FilterArg.CARD_ID);
			if (requiredCardId.equalsIgnoreCase("EVENT_CARD")) {
				requiredCardId = context.getEventCard().getCardId();
			}
			return card.getCardId().equalsIgnoreCase(requiredCardId);
		}

		if (desc.getString(FilterArg.CUSTOM).equals("TARGETING")) {
			if (card.getCardType().equals(CardType.SPELL)) {
				SpellCard spell = (SpellCard) card;
				if (spell.getTargetRequirement().equals(TargetSelection.NONE)) {
					return false;
				}
			} else if (card.getCardType().equals(CardType.MINION)) {
				MinionCard minionCard = (MinionCard) card;
				if (minionCard.hasBattlecry() && !minionCard.getBattlecry().getTargetSelection().equals(TargetSelection.NONE)) {
					return false;
				}
			}
		}

		return true;
	}

}
