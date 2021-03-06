package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.GroupCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.minions.RelativeToSource;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.targeting.CardLocation;
import net.demilich.metastone.game.targeting.EntityReference;

public class SpellUtils {

	public static void castChildSpell(GameContext context, Player player, SpellDesc spell, Entity source, Entity target, Entity output) {
		// card may be null (i.e. try to draw from deck, but already in
		// fatigue)
		if (output == null) {
			return;
		} else if (output.getEntityType().equals(EntityType.CARD)) {
			if (((Card)output).getLocation().equals(CardLocation.GRAVEYARD)) {
				return;
			}
		}
		if (spell == null) {
			return;
		}

		context.getOutputStack().push(output.getReference());
		castChildSpell(context, player, spell, source, target);
		context.getOutputStack().pop();
	}

	public static void castChildSpell(GameContext context, Player player, SpellDesc spell, Entity source, Entity target) {
		EntityReference sourceReference = source != null ? source.getReference() : null;
		EntityReference targetReference = spell.getTarget();
		if (targetReference == null && target != null) {
			targetReference = target.getReference();
		}
		context.getLogic().castSpell(player.getId(), spell, sourceReference, targetReference, true);
	}

	public static boolean evaluateOperation(ComparisonOperation operation, int actualValue, int targetValue) {
		switch (operation) {
		case EQUAL:
			return actualValue == targetValue;
		case GREATER:
			return actualValue > targetValue;
		case GREATER_OR_EQUAL:
			return actualValue >= targetValue;
		case HAS:
			return actualValue > 0;
		case LESS:
			return actualValue < targetValue;
		case LESS_OR_EQUAL:
			return actualValue <= targetValue;
		case MOD_2_EQUAL:
			return actualValue % 2 == targetValue;
		}
		return false;
	}

	public static CardList getCards(CardList source, Predicate<Card> filter) {
		CardList result = new CardList();
		for (Card card : source) {
			if (filter == null || filter.test(card)) {
				result.add(card);
			}
		}
		return result;
	}
	
	public static Card getCard(GameContext context, SpellDesc spell) {
		Card card = null;
		String cardName = (String) spell.get(SpellArg.CARD);
		card = context.getCardById(cardName);
		if (spell.get(SpellArg.CARD).toString().toUpperCase().equals("PENDING_CARD")) {
			card = (Card) context.getPendingCard();
		} else if (spell.get(SpellArg.CARD).toString().toUpperCase().equals("EVENT_CARD")) {
			card = (Card) context.getEventCard();
		}
		return card;
	}

	public static CardList getCards(GameContext context, SpellDesc spell, Player player) {
		CardList cards = new CardList();
		if (spell.contains(SpellArg.CARDS)) {
			String[] cardNames = (String[]) spell.get(SpellArg.CARDS);
			for (int i = 0; i < cardNames.length; i++) {
				cards.add(context.getCardById(cardNames[i]));
			}
		} else if (spell.contains(SpellArg.CARD_SOURCE)) {
			CardSource cardSource = (CardSource) spell.get(SpellArg.CARD_SOURCE);
			cards = cardSource.getCards(context, player);
		} else {
			cards.add(context.getCardById((String) spell.get(SpellArg.CARD)));
		}
		EntityFilter filter = spell.getEntityFilter();
		if (filter != null) {
			cards.removeAll(card -> !filter.matches(context, player, card));
		}
		return cards;
	}

	public static SpellDesc[] getGroup(GameContext context, SpellDesc spell) {
		Card card = null;
		String cardName = (String) spell.get(SpellArg.GROUP);
		card = context.getCardById(cardName);
		if (card != null && card instanceof GroupCard) {
			GroupCard groupCard = (GroupCard) card;
			return groupCard.getGroup();
		}
		return null;
	}
	
	public static DiscoverAction getDiscover(GameContext context, Player player, SpellDesc desc, CardList cards) {
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		List<GameAction> discoverActions = new ArrayList<>();
		for (Card card : cards) {
			SpellDesc spellClone = spell.addArg(SpellArg.CARD, card.getCardId());
			DiscoverAction discover = DiscoverAction.createDiscover(spellClone);
			discover.setCard(card);
			discover.setActionSuffix(card.getName());
			discoverActions.add(discover);
		}
		if (discoverActions.size() == 0) {
			return null;
		}
		
		if (context.getLogic().attributeExists(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION)) {
			return (DiscoverAction) discoverActions.get(context.getLogic().random(discoverActions.size()));
		} else {
			return (DiscoverAction) player.getBehaviour().requestAction(context, player, discoverActions);
		}
	}

	public static DiscoverAction getSpellDiscover(GameContext context, Player player, SpellDesc desc, List<SpellDesc> spells) {
		List<GameAction> discoverActions = new ArrayList<>();
		for (SpellDesc spell : spells) {
			DiscoverAction discover = DiscoverAction.createDiscover(spell);
			discover.setName(spell.getString(SpellArg.NAME));
			discover.setDescription(spell.getString(SpellArg.DESCRIPTION));
			discover.setActionSuffix((String) spell.get(SpellArg.NAME));
			discoverActions.add(discover);
		}
		
		if (context.getLogic().attributeExists(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION)) {
			return (DiscoverAction) discoverActions.get(context.getLogic().random(discoverActions.size()));
		} else {
			return (DiscoverAction) player.getBehaviour().requestAction(context, player, discoverActions);
		}
	}

	public static Card getRandomCard(CardList source, Predicate<Card> filter) {
		CardList result = getCards(source, filter);
		if (result.isEmpty()) {
			return null;
		}
		return result.getRandom();
	}
	
	public static HeroClass getRandomHeroClass() {
		HeroClass[] values = HeroClass.values();
		List<HeroClass> heroClasses = new ArrayList<HeroClass>();
		for (HeroClass heroClass : values) {
			if (heroClass.isBaseClass()) {
				heroClasses.add(heroClass);
			}
		}
		return heroClasses.get(ThreadLocalRandom.current().nextInt(heroClasses.size()));
	}
	
	public static HeroClass getRandomHeroClassExcept(HeroClass... heroClassesExcluded) {
		HeroClass[] values = HeroClass.values();
		List<HeroClass> heroClasses = new ArrayList<HeroClass>();
		for (HeroClass heroClass : values) {
			if (heroClass.isBaseClass()) {
				heroClasses.add(heroClass);
				for (HeroClass heroClassExcluded : heroClassesExcluded) {
					if (heroClassExcluded == heroClass) {
						heroClasses.remove(heroClass);
					}
				}
			}
		}
		return heroClasses.get(ThreadLocalRandom.current().nextInt(heroClasses.size()));
	}

	public static <T> T getRandomTarget(List<T> targets) {
		int randomIndex = ThreadLocalRandom.current().nextInt(targets.size());
		return targets.get(randomIndex);
	}

	public static List<Actor> getValidRandomTargets(List<Entity> targets) {
		List<Actor> validTargets = new ArrayList<Actor>();
		for (Entity entity : targets) {
			Actor actor = (Actor) entity;
			if (!actor.isDestroyed() || actor.getEntityType() == EntityType.HERO) {
				validTargets.add(actor);
			}

		}
		return validTargets;
	}

	public static List<Entity> getValidTargets(GameContext context, Player player, List<Entity> allTargets, EntityFilter filter) {
		if (filter == null) {
			return allTargets;
		}
		List<Entity> validTargets = new ArrayList<>();
		for (Entity entity : allTargets) {
			if (filter.matches(context, player, entity)) {
				validTargets.add(entity);
			}
		}
		return validTargets;
	}

	public static int hasHowManyOfRace(Player player, Race race) {
		int count = 0;
		for (Summon summon : player.getSummons()) {
			if (summon.getRace() == race) {
				count++;
			}
		}
		return count;
	}
	
	public static boolean highlanderDeck(Player player) {
		List<String> cards = new ArrayList<String>();
		for (Card card : player.getDeck()) {
			if (cards.contains(card.getCardId())) {
				return false;
			}
			cards.add(card.getCardId());
		}
		return true;
	}

	public static boolean holdsCardOfType(Player player, CardType cardType) {
		for (Card card : player.getHand()) {
			if (card.getCardType().isCardType(cardType)) {
				return true;
			}
		}
		return false;
	}

	public static boolean holdsMinionOfRace(Player player, Race race) {
		for (Card card : player.getHand()) {
			if (card.getAttribute(Attribute.RACE) == race) {
				return true;
			}
		}
		return false;
	}

	public static int howManyMinionsDiedThisTurn(GameContext context, Player player) {
		int currentTurn = context.getTurn();
		int count = 0;
		for (Entity deadEntity : player.getGraveyard()) {
			if (deadEntity.getEntityType() != EntityType.MINION) {
				continue;
			}

			if (deadEntity.getAttributeValue(Attribute.DIED_ON_TURN) == currentTurn) {
				count++;
			}

		}
		
		return count;
	}
	
	public static int getBoardPosition(GameContext context, Player player, SpellDesc desc, Entity source) {
		final int UNDEFINED = -1;
		int boardPosition = desc.getInt(SpellArg.BOARD_POSITION_ABSOLUTE, UNDEFINED);
		if (boardPosition != UNDEFINED) {
			return boardPosition;
		}
		RelativeToSource relativeBoardPosition = (RelativeToSource) desc.get(SpellArg.BOARD_POSITION_RELATIVE);
		if (relativeBoardPosition == null) {
			return UNDEFINED;
		}

		int sourcePosition = context.getBoardPosition((Summon) source);
		if (sourcePosition == UNDEFINED) {
			return UNDEFINED;
		}
		switch (relativeBoardPosition) {
		case LEFT:
			return sourcePosition;
		case RIGHT:
			return sourcePosition + 1;
		default:
			return UNDEFINED;
		}
	}

	private SpellUtils() {
	}

}
