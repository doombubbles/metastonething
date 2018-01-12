package net.demilich.metastone.game.logic;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import net.demilich.metastone.game.behaviour.human.HumanBehaviour;
import net.demilich.metastone.game.entities.minions.*;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.QuestCard;
import net.demilich.metastone.game.cards.SecretCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.heroes.powers.HeroPower;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.SpellFactory;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;
import net.demilich.metastone.game.spells.trigger.types.Quest;
import net.demilich.metastone.game.spells.trigger.types.Secret;
import net.demilich.metastone.game.statistics.Statistic;
import net.demilich.metastone.game.targeting.CardLocation;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.utils.MathUtils;
import org.w3c.dom.Attr;

import javax.smartcardio.ATR;

public class GameLogic implements Cloneable, Serializable {

	public static Logger logger = LoggerFactory.getLogger(GameLogic.class);

	public static final int MAX_PLAYERS = 2;
	public static final int MAX_MINIONS = 7;
	public static final int MAX_HAND_CARDS = 10;
	public static final int MAX_HERO_HP = 30;
	public static final int STARTER_CARDS = 3;
	public static final int MAX_MANA = 10;
	public static final int MAX_QUESTS = 1;
	public static final int MAX_SECRETS = 5;
	public static final int DECK_SIZE = 30;
	public static final int MAX_DECK_SIZE = 60;
	public static final int TURN_LIMIT = 100;

	public static final int WINDFURY_ATTACKS = 2;
	public static final int MEGA_WINDFURY_ATTACKS = 4;

	public static final String TEMP_CARD_LABEL = "temp_card_id_";

	private static final int INFINITE = -1;

	private static boolean hasPlayerLost(Player player) {
		return player.getHero().getHp() < 1 || player.getHero().hasAttribute(Attribute.DESTROYED)
				|| player.hasAttribute(Attribute.DESTROYED);
	}

	private final TargetLogic targetLogic = new TargetLogic();
	private final ActionLogic actionLogic = new ActionLogic();
	private final SpellFactory spellFactory = new SpellFactory();
	private final IdFactory idFactory;
	private GameContext context;

	private boolean loggingEnabled = true;

	// DEBUG
	private final int MAX_HISTORY_ENTRIES = 100;
	private Queue<String> debugHistory = new LinkedList<>();

	public GameLogic() {
		idFactory = new IdFactory();
	}

	private GameLogic(IdFactory idFactory) {
		this.idFactory = idFactory;
	}

	public void addGameEventListener(Player player, IGameEventListener gameEventListener, Entity target) {
		debugHistory.add("Player " + player.getId() + " has set event listener " + gameEventListener.getClass().getName() + " from entity " + target.getName() + "[Reference ID: " + target.getId() + "]");
		gameEventListener.setHost(target);
		if (!gameEventListener.hasPersistentOwner() || gameEventListener.getOwner() == -1) {
			gameEventListener.setOwner(player.getId());
		}

		gameEventListener.onAdd(context);
		context.addTrigger(gameEventListener);
		log("New spelltrigger was added for {} on {}: {}", player.getName(), target, gameEventListener);
	}

	public void addManaModifier(Player player, CardCostModifier cardCostModifier, Entity target) {
		context.getCardCostModifiers().add(cardCostModifier);
		addGameEventListener(player, cardCostModifier, target);
	}

	public void afterCardPlayed(int playerId, CardReference cardReference) {
		Player player = context.getPlayer(playerId);

		player.modifyAttribute(Attribute.COMBO, +1);
		Card card = context.resolveCardReference(cardReference);
		
		card.removeAttribute(Attribute.MANA_COST_MODIFIER);
	}

	public int applyAmplify(Player player, int baseValue, Attribute attribute) {
		int amplify = getTotalAttributeMultiplier(player, attribute);
		return baseValue * amplify;
	}

	public void applyAttribute(Entity entity, Attribute attr) {
		if (attr == Attribute.MEGA_WINDFURY && entity.hasAttribute(Attribute.WINDFURY) && !entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, MEGA_WINDFURY_ATTACKS - WINDFURY_ATTACKS);
		} else
			if (attr == Attribute.WINDFURY && !entity.hasAttribute(Attribute.WINDFURY) && !entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, WINDFURY_ATTACKS - 1);
		} else if (attr == Attribute.MEGA_WINDFURY && !entity.hasAttribute(Attribute.WINDFURY) && !entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, MEGA_WINDFURY_ATTACKS - 1);
		}
		entity.setAttribute(attr);
		context.fireGameEvent(new AttributeGainedEvent(context, entity, attr));
		log("Applying attr {} to {}", attr, entity);
	}

	/**
	 * Applies hero power damage increases
	 * @param player
	 * 			The Player to grab additional hero power damage from
	 * @param baseValue
	 * 			The base damage the hero power does
	 * @return
	 * 			Increased hero power damage
	 */
	public int applyHeroPowerDamage(Player player, int baseValue) {
		int spellpower = getTotalAttributeValue(player, Attribute.HERO_POWER_DAMAGE);
		return baseValue + spellpower;
	}

	/**
	 * Applies spell damage increases
	 * @param player
	 * 			The Player to grab the additional spell damage from
	 * @param source
	 * 			The source Card
	 * @param baseValue
	 * 			The base damage the spell does
	 * @return
	 * 			Increased spell damage
	 */
	public int applySpellpower(Player player, Entity source, int baseValue) {
		int spellpower = getTotalAttributeValue(player, Attribute.SPELL_DAMAGE) + getTotalAttributeValue(context.getOpponent(player), Attribute.OPPONENT_SPELL_DAMAGE);
		if (source.hasAttribute(Attribute.SPELL_DAMAGE_MULTIPLIER)) {
			spellpower *= source.getAttributeValue(Attribute.SPELL_DAMAGE_MULTIPLIER);
		}
		for (Summon summon : player.getSummons()) {
			if (summon.hasAttribute(Attribute.DOUBLE_RACE_SPELL_DAMAGE) && summon.getAttribute(Attribute.DOUBLE_RACE_SPELL_DAMAGE) == source.getAttribute(Attribute.RACE)) {
				spellpower *= 2;
			}
		}
		return baseValue + spellpower;
	}

	/**
	 * Assigns an ID to each Card in a given deck
	 * @param cardCollection
	 * 		The Deck to assign IDs to
	 */
	private void assignCardIds(CardCollection cardCollection) {
		for (Card card : cardCollection) {
			card.setId(idFactory.generateId());
			card.setLocation(CardLocation.DECK);
		}
	}

	public boolean attributeExists(Attribute attr) {
		for (Player player : context.getPlayers()) {
			if (player.getHero().hasAttribute(attr)) {
				return true;
			}
			for (Entity summon : player.getSummons()) {
				if (summon.hasAttribute(attr) && !summon.hasAttribute(Attribute.PENDING_DESTROY)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean canPlayCard(int playerId, CardReference cardReference) {
		Player player = context.getPlayer(playerId);
		Card card = context.resolveCardReference(cardReference);
		int manaCost = getModifiedManaCost(player, card);
		List<CardType> cardTypes = new ArrayList<>();
		if (hasAttribute(player, Attribute.CARD_TYPE_COSTS_HEALTH)) {
			List<Object> results = getAttributes(player, Attribute.CARD_TYPE_COSTS_HEALTH);
			for (Object object : results) {
				CardType cardType = (CardType) object;
				cardTypes.add(cardType);
			}
			if (cardTypes.contains(card.getCardType()) && player.getHero().getEffectiveHp() < manaCost && !hasAttribute(player, Attribute.IMMUNE_HERO) && !player.hasAttribute(Attribute.IMMUNE)) {
				return false;
			}
		}
		if (card.hasAttribute(Attribute.COSTS_HEALTH) && player.getHero().getEffectiveHp() < manaCost) {
			return false; 
		}
		else if (card.getCardType().isCardType(CardType.MINION)
				&& (card.getAttribute(Attribute.RACE) == Race.MURLOC || card.getAttribute(Attribute.RACE) == Race.ALL)
				&& player.hasAttribute(Attribute.MURLOCS_COST_HEALTH)
				&& player.getHero().getEffectiveHp() < manaCost) {
			return false;
		} else if (player.getMana() < manaCost && manaCost != 0
				&& !(cardTypes.contains(card.getCardType()))
				&& !(((Race) card.getAttribute(Attribute.RACE) == Race.MURLOC || (Race) card.getAttribute(Attribute.RACE) == Race.ALL) && player.hasAttribute(Attribute.MURLOCS_COST_HEALTH))
				&& !card.hasAttribute(Attribute.COSTS_HEALTH)) {
			return false;
		}
		if (card.getCardType().isCardType(CardType.HERO_POWER)) {
			HeroPower power = (HeroPower) card;
			int heroPowerUsages = getGreatestAttributeValue(player, Attribute.HERO_POWER_USAGES);
			if (heroPowerUsages == 0) {
				heroPowerUsages = 1;
			} 
			if (hasAttribute(player, Attribute.KRYPTONITE) || hasAttribute(context.getOpponent(player), Attribute.KRYPTONITE)) {
				return false;
			}
			if (heroPowerUsages != INFINITE && power.hasBeenUsed() >= heroPowerUsages) {
				return false;
			}
		} else if (card.getCardType().isCardType(CardType.MINION)) {
			return canSummonMoreMinions(player);
		}
		if (card instanceof SecretCard) {
			SecretCard secretCard = (SecretCard) card;
			return secretCard.canBeCast(context, player);
		}
		if (card instanceof SpellCard) {
			SpellCard spellCard = (SpellCard) card;
			return spellCard.canBeCast(context, player);
		}
		return true;
	}

	public boolean canPlayQuest(Player player, QuestCard card) {
		return player.getSecrets().size() < MAX_SECRETS && player.getQuests().size() < MAX_QUESTS && !player.getQuests().contains(card.getCardId());
	}

	public boolean canPlaySecret(Player player, SecretCard card) {
		if (hasAttribute(player, Attribute.INSTANT_TRAPS)) {
			TargetSelection targetRequirement = card.getInstant().getTargetSelection();
			//info("" + targetRequirement);
			if (targetRequirement == null) {
				targetRequirement = TargetSelection.NONE;
			}
			Player opponent = context.getOpponent(player);
			switch (targetRequirement) {
			case ENEMY_MINIONS:
				return context.getMinionCount(opponent) > 0;
			case FRIENDLY_MINIONS:
				return context.getMinionCount(player) > 0;
			case MINIONS:
				return context.getTotalMinionCount() > 0;
			default:
				break;
			}
			return true;
		} else return player.getSecrets().size() < MAX_SECRETS && !player.getSecrets().contains(card.getCardId());
	}

	public boolean canSummonMoreMinions(Player player) {
		return player.getSummons().size() < MAX_MINIONS;
	}

	public void castChooseOneSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference, String cardId) {
		Player player = context.getPlayer(playerId);
		Entity source = null;
		if (sourceReference != null) {
			try {
				source = context.resolveSingleTarget(sourceReference);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error resolving source entity while casting spell: " + spellDesc);
			}
		}
		EntityReference spellTarget = spellDesc.hasPredefinedTarget() ? spellDesc.getTarget() : targetReference;
		List<Entity> targets = targetLogic.resolveTargetKey(context, player, source, spellTarget);
		Card sourceCard = null;
		SpellCard chosenCard = (SpellCard) context.getCardById(cardId);
		sourceCard = source.getEntityType() == EntityType.CARD ? (Card) source : null;
		if (!spellDesc.hasPredefinedTarget() && targets != null && targets.size() == 1) {
			if (chosenCard.getTargetRequirement() != TargetSelection.NONE) {
				context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
				context.getEnvironment().put(Environment.CHOOSE_ONE_CARD, chosenCard.getCardId());
				GameEvent spellTargetEvent = new TargetAcquisitionEvent(context, playerId, ActionType.SPELL, chosenCard, targets.get(0));
				context.fireGameEvent(spellTargetEvent);
				Entity targetOverride = context
						.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
				if (targetOverride != null && targetOverride.getId() != IdFactory.UNASSIGNED) {
					targets.remove(0);
					targets.add(targetOverride);
					spellDesc = spellDesc.addArg(SpellArg.FILTER, null);
					log("Target for spell {} has been changed! New target {}", chosenCard, targets.get(0));
				}
			}
		}
		try {
			Spell spell = spellFactory.getSpell(spellDesc);
			spell.cast(context, player, spellDesc, source, targets);
		} catch (Exception e) {
			if (source != null) {
				logger.error("Error while playing card: " + source.getName());
			}
			logger.error("Error while casting spell: " + spellDesc);
			panicDump();
			e.printStackTrace();
		}

		context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
		context.getEnvironment().remove(Environment.CHOOSE_ONE_CARD);

		checkForDeadEntities();
		if (targets == null || targets.size() != 1) {
			context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, null));
		} else {
			context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, targets.get(0)));
		}
	}

	public void castSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference, boolean childSpell) {
		castSpell(playerId, spellDesc, sourceReference, targetReference, TargetSelection.NONE, childSpell);
	}

	public void castSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference, TargetSelection targetSelection, boolean childSpell) {
		Player player = context.getPlayer(playerId);
		Entity source = null;
		if (sourceReference != null) {
			try {
				source = context.resolveSingleTarget(sourceReference);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error resolving source entity while casting spell: " + spellDesc);
			}

		}
		//SpellCard spellCard = null;
		EntityReference spellTarget = spellDesc.hasPredefinedTarget() ? spellDesc.getTarget() : targetReference;
		List<Entity> targets = targetLogic.resolveTargetKey(context, player, source, spellTarget);
		// target can only be changed when there is one target
		// note: this code block is basically exclusively for the SpellBender
		// Secret, but it can easily be expanded if targets of area of effect
		// spell should be changeable as well
		Card sourceCard = null;
		if (source != null) {
			sourceCard = source.getEntityType() == EntityType.CARD ? (Card) source : null;
		}
		if (sourceCard != null && sourceCard.getCardType().isCardType(CardType.SPELL) && !spellDesc.hasPredefinedTarget() && targets != null
				&& targets.size() == 1) {
			if (sourceCard.getCardType().isCardType(CardType.SPELL) && targetSelection != TargetSelection.NONE && !childSpell) {
				GameEvent spellTargetEvent = new TargetAcquisitionEvent(context, playerId, ActionType.SPELL, sourceCard, targets.get(0));
				context.fireGameEvent(spellTargetEvent);
				Entity targetOverride = context
						.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
				if (targetOverride != null && targetOverride.getId() != IdFactory.UNASSIGNED) {
					targets.remove(0);
					targets.add(targetOverride);
					spellDesc = spellDesc.addArg(SpellArg.FILTER, null);
					log("Target for spell {} has been changed! New target {}", sourceCard, targets.get(0));
				}
			}

		}
		try {
			Spell spell = spellFactory.getSpell(spellDesc);
			spell.cast(context, player, spellDesc, source, targets);
		} catch (Exception e) {
			if (source != null) {
				logger.error("Error while playing card: " + source.getName());
			}
			logger.error("Error while casting spell: " + spellDesc);
			panicDump();
			e.printStackTrace();
		}

		if (sourceCard != null && sourceCard.getCardType().isCardType(CardType.SPELL) && !childSpell) {
			context.getEnvironment().remove(Environment.TARGET_OVERRIDE);

			checkForDeadEntities();
			if (targets == null || targets.size() != 1) {
				context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, null));
			} else {
				context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, targets.get(0)));
				if (targets.get(0).getEntityType() == EntityType.MINION && targets.get(0).getOwner() == playerId) {
					player.spellsCastOnFriendlies.add(sourceCard);
				}
			}
		}
	}

	public void changeHero(Player player, Hero hero) {
		int attacks = player.getHero().getAttributeValue(Attribute.NUMBER_OF_ATTACKS);
		hero.setId(player.getHero().getId());
		if (hero.getHeroClass() == null || hero.getHeroClass() == HeroClass.ANY) {
			hero.setHeroClass(player.getHero().getHeroClass());
		}

		log("{}'s hero has been changed to {}", player.getName(), hero);
		hero.setOwner(player.getId());
		hero.setWeapon(player.getHero().getWeapon());
		player.setHero(hero);
		player.getHero().setAttribute(Attribute.NUMBER_OF_ATTACKS, attacks);
	}

	public void checkForDeadEntities() {
		checkForDeadEntities(0);
	}

	/**
	 * Checks all player minions and weapons for destroyed actors and proceeds
	 * with the removal in correct order
	 */
	public void checkForDeadEntities(int i) {
		// sanity check, this method should never call itself that often
		if (i > 20) {
			panicDump();
			throw new RuntimeException("Infinite death checking loop");
		}

		List<Actor> destroyList = new ArrayList<>();
		for (Player player : context.getPlayers()) {

			if (player.getHero().isDestroyed() || player.hasAttribute(Attribute.DESTROYED)) {
				destroyList.add(player.getHero());
			}

			for (Summon summon : player.getSummons()) {
				if (summon.isDestroyed()) {
					destroyList.add(summon);
				}
			}
			if (player.getHero().getWeapon() != null && player.getHero().getWeapon().isDestroyed()) {
				destroyList.add(player.getHero().getWeapon());
			}
		}

		if (destroyList.isEmpty()) {
			return;
		}

		// sort the destroyed actors by their id. This implies that actors with a lower id entered the game ealier than those with higher ids!
		Collections.sort(destroyList, (a1, a2) -> Integer.compare(a1.getId(), a2.getId()));
		// this method performs the actual removal
		destroy(destroyList.toArray(new Actor[0]));
		if (context.gameDecided()) {
			return;
		}
		// deathrattles have been resolved, which may lead to other actors being destroyed now, so we need to check again
		checkForDeadEntities(i + 1);
	}

	@Override
	public GameLogic clone() {
		GameLogic clone = new GameLogic(idFactory.clone());
		clone.debugHistory = new LinkedList<>(debugHistory);
		return clone;
	}

	public int damage(Player player, Actor target, int baseDamage, Entity source) {
		return damage(player, target, baseDamage, source, false);
	}

	public int damage(Player player, Actor target, int baseDamage, Entity source, boolean ignoreSpellDamage) {
		return damage(player, target, baseDamage, source, ignoreSpellDamage, null);
	}

	public int damage(Player player, Actor target, int baseDamage, Entity source, boolean ignoreSpellDamage, EntityReference excess) {
		// sanity check to prevent StackOverFlowError with Mistress of Pain +
		// Auchenai Soulpriest
		if (target.getHp() < -100) {
			return 0;
		}
		int damage = baseDamage;
		Card sourceCard = source != null && source.getEntityType() == EntityType.CARD ? (Card) source : null;
		if (!ignoreSpellDamage && sourceCard != null) {
			if (sourceCard.getCardType().isCardType(CardType.SPELL)) {
				damage = applySpellpower(player, source, baseDamage) + sourceCard.getAttributeValue(Attribute.SPELL_DAMAGE) + player.getAttributeValue(Attribute.SPELL_DAMAGE);
			} else if (sourceCard.getCardType().isCardType(CardType.HERO_POWER)) {
				damage = applyHeroPowerDamage(player, damage);
				if (hasAttribute(player, Attribute.FREEZE_POWER)) {
					applyAttribute(target, Attribute.FROZEN);
				}
			}
			if (sourceCard.getCardType().isCardType(CardType.SPELL) || sourceCard.getCardType().isCardType(CardType.HERO_POWER)) {
				damage = applyAmplify(player, damage, Attribute.SPELL_AMPLIFY_MULTIPLIER);
			}
		}
		int damageDealt = 0;
		if (target.hasAttribute(Attribute.TAKE_DOUBLE_DAMAGE)) {
			damage *= 2;
		}

		if (excess != null) {
			int excessDamage = Math.max(0, damage - target.getAttributeValue(Attribute.HP));
			damage -= excessDamage;
			EntityReference targetKey = excess;
			for (Entity entity : context.resolveTarget(player, target, targetKey)) {
				if (entity != target) {
					damage(player, (Actor) entity, excessDamage, source, true, null);
				}
			}
		}

		context.getDamageStack().push(damage);
		context.fireGameEvent(new PreDamageEvent(context, target, source));
		damage = context.getDamageStack().pop();
		if (damage > 0) {
			source.removeAttribute(Attribute.STEALTH);
		}
		switch (target.getEntityType()) {
		case MINION:
			damageDealt = damageMinion((Actor) target, damage);
			break;
		case HERO:
			damageDealt = damageHero((Hero) target, damage);
			break;
		default:
			break;
		}

		target.setAttribute(Attribute.LAST_HIT, damageDealt);
		if (damageDealt > 0) {
			DamageEvent damageEvent = new DamageEvent(context, target, source, damageDealt);
			context.fireGameEvent(damageEvent);
			player.getStatistics().damageDealt(damageDealt);
			
			if (target.getEntityType() == EntityType.MINION) {
				if (source.hasAttribute(Attribute.POISONOUS)) {
					destroy(target);
				} else if (sourceCard != null) {
					if (sourceCard.hasAttribute(Attribute.POISONOUS)) {
						destroy(target);
					}
				}
			}
			
		} else if (damageDealt < 0) {
			HealEvent healEvent = new HealEvent(context, player.getId(), target, damage * -1);
			context.fireGameEvent(healEvent);
			player.getStatistics().heal(damage * -1);
		}
		
		if (source.getEntityType() == EntityType.CARD) {
			if (((Card) source).getCardType().equals(CardType.HERO_POWER)) {
				source = player.getHero().getHeroPower();
			}
		}
		
		if (source.hasAttribute(Attribute.LIFESTEAL)) {
			if (hasAttribute(player, Attribute.INVERT_HEALING)) {
				damage(player, context.getPlayer(source.getOwner()).getHero(), damageDealt, context.getPlayer(source.getOwner()).getHero());
			} else heal(player, context.getPlayer(source.getOwner()).getHero() , damageDealt, source);
		} else if (sourceCard != null) {
			if (sourceCard.hasAttribute(Attribute.LIFESTEAL)) {
				info("yo2");
				if (hasAttribute(player, Attribute.INVERT_HEALING)) {
					damage(player, context.getPlayer(source.getOwner()).getHero(), damageDealt, context.getPlayer(source.getOwner()).getHero());
				} else heal(player, context.getPlayer(source.getOwner()).getHero() , damageDealt, source);
			}
		}
		
		

		return damageDealt;
	}

	private int damageHero(Hero hero, int damage) {
		if (hero.hasAttribute(Attribute.DIVINE_SHIELD)) {
			removeAttribute(hero, Attribute.DIVINE_SHIELD);
			log("{}'s DIVINE SHIELD absorbs the damage", hero);
			return 0;
		}
		if (hero.hasAttribute(Attribute.IMMUNE) || hasAttribute(context.getPlayer(hero.getOwner()), Attribute.IMMUNE_HERO)) {
			log("{} is IMMUNE and does not take damage", hero);
			return 0;
		}
		
		
		if (damage > 0) {
			int effectiveHp = hero.getHp() + hero.getArmor();
			hero.modifyArmor(-damage);
			int newHp = Math.min(hero.getHp(), effectiveHp - damage);
			hero.setHp(newHp);
		} else {
			hero.setHp(Math.min(hero.getMaxHp(), hero.getHp() - damage));
		}
		
		
		
		log(hero.getName() + " receives " + damage + " damage, hp now: " + hero.getHp() + "(" + hero.getArmor() + ")");
		if (context.getActivePlayerId() == hero.getOwner()) {
			context.getPlayer(hero.getOwner()).getStatistics().damageThisTurn(damage);
		}

		return damage;
	}

	private int damageMinion(Actor minion, int damage) {
		if (minion.hasAttribute(Attribute.DIVINE_SHIELD)) {
			removeAttribute(minion, Attribute.DIVINE_SHIELD);
			log("{}'s DIVINE SHIELD absorbs the damage", minion);
			return 0;
		}
		if (minion.hasAttribute(Attribute.IMMUNE)) {
			log("{} is IMMUNE and does not take damage", minion);
			return 0;
		}
		if (damage >= minion.getHp() && minion.hasAttribute(Attribute.CANNOT_REDUCE_HP_BELOW_1)) {
			damage = minion.getHp() - 1;
		}

		log("{} is damaged for {}", minion, damage);
		minion.setHp(minion.getHp() - damage);
		handleEnrage(minion);
		return damage;
	}

	public void destroy(Actor... targets) {
		int[] boardPositions = new int[targets.length];

		for (int i = 0; i < targets.length; i++) {
			Actor target = targets[i];
			removeSpellTriggers(target, false);
			Player owner = context.getPlayer(target.getOwner());
			context.getPlayer(target.getOwner()).getGraveyard().add(target);

			int boardPosition = owner.getSummons().indexOf(target);
			boardPositions[i] = boardPosition;
		}

		for (int i = 0; i < targets.length; i++) {
			Actor target = targets[i];
			log("{} is destroyed", target);
			Player owner = context.getPlayer(target.getOwner());
			owner.getSummons().remove(target);
		}

		for (int i = 0; i < targets.length; i++) {
			Actor target = targets[i];
			Player owner = context.getPlayer(target.getOwner());
			switch (target.getEntityType()) {
			case HERO:
				log("Hero {} has been destroyed.", target.getName());
				applyAttribute(target, Attribute.DESTROYED);
				applyAttribute(context.getPlayer(target.getOwner()), Attribute.DESTROYED);
				break;
			case MINION:
				destroyMinion((Minion) target);
				break;
			case WEAPON:
				destroyWeapon((Weapon) target);
				break;
			case ANY:
			case PERMANENT:
				break;
			default:
				logger.error("Trying to destroy unknown entity type {}", target.getEntityType());
				break;
			}

			resolveDeathrattles(owner, target, boardPositions[i]);
		}
		
		for (Actor target : targets) {
			removeSpellTriggers(target, true);
		}

		context.fireGameEvent(new BoardChangedEvent(context));
	}

	private void destroyMinion(Minion minion) {
		context.getEnvironment().put(Environment.KILLED_MINION, minion.getReference());
		KillEvent killEvent = new KillEvent(context, minion);
		context.fireGameEvent(killEvent);
		context.getEnvironment().remove(Environment.KILLED_MINION);

		minion.setAttribute(Attribute.DESTROYED);
		minion.setAttribute(Attribute.DIED_ON_TURN, context.getTurn());
	}

	private void destroyWeapon(Weapon weapon) {
		Player owner = context.getPlayer(weapon.getOwner());
		// resolveDeathrattles(owner, weapon);
		if (owner.getHero().getWeapon() != null && owner.getHero().getWeapon().getId() == weapon.getId()) {
			owner.getHero().setWeapon(null);
		}
		weapon.onUnequip(context, owner);
		owner.getGraveyard().add(weapon);
		context.fireGameEvent(new WeaponDestroyedEvent(context, weapon));
	}

	public int determineBeginner(int... playerIds) {
		return ThreadLocalRandom.current().nextBoolean() ? playerIds[0] : playerIds[1];
	}

	public void discardCard(Player player, Card card) {
		logger.debug("{} discards {}", player.getName(), card);
		// only a 'real' discard should fire a DiscardEvent
		if (card.getLocation() == CardLocation.HAND) {
			context.fireGameEvent(new DiscardEvent(context, player.getId(), card));
		}

		removeCard(player.getId(), card);
	}

	public Card drawCard(int playerId, Entity source) {
		Player player = context.getPlayer(playerId);
		CardCollection deck = player.getDeck();
		if (deck.isEmpty()) {
			Hero hero = player.getHero();
			int fatigue = player.hasAttribute(Attribute.FATIGUE) ? player.getAttributeValue(Attribute.FATIGUE) : 0;
			fatigue++;
			player.setAttribute(Attribute.FATIGUE, fatigue);
			damage(player, hero, fatigue, hero);
			log("{}'s deck is empty, taking {} fatigue damage!", player.getName(), fatigue);
			player.getStatistics().fatigueDamage(fatigue);
			return null;
		}

		Card card = deck.getRandom();
		return drawCard(playerId, card, source);
	}

	public Card drawCard(int playerId, Card card, Entity source) {
		Player player = context.getPlayer(playerId);
		player.getStatistics().cardDrawn();
		player.getDeck().remove(card);
		receiveCard(playerId, card, source, true);
		return card;
	}

	public void drawSetAsideCard(int playerId, Card card) {
		if (card.getId() == IdFactory.UNASSIGNED) {
			card.setId(idFactory.generateId());
		}
		card.setOwner(playerId);
		Player player = context.getPlayer(playerId);
		player.getSetAsideZone().add(card);
	}

	public void endTurn(int playerId) {
		Player player = context.getPlayer(playerId);

		Hero hero = player.getHero();
		hero.removeAttribute(Attribute.TEMPORARY_ATTACK_BONUS);
		hero.removeAttribute(Attribute.HERO_POWER_USAGES);
		handleFrozen(hero);
		for (Summon summon : player.getSummons()) {
			summon.removeAttribute(Attribute.TEMPORARY_ATTACK_BONUS);
			handleFrozen(summon);
		}
		player.removeAttribute(Attribute.COMBO);
		player.getHero().removeAttribute(Attribute.CUSTOM_7);
		player.getStatistics().set(Statistic.DAMAGE_THIS_TURN, 0L);
		hero.activateWeapon(false);
		log("{} ends his turn.", player.getName());
		context.fireGameEvent(new TurnEndEvent(context, playerId));
		if (hasAttribute(player, Attribute.DOUBLE_END_TURN)) {
			context.fireGameEvent(new TurnEndEvent(context, playerId));
		}
		for (Summon summon: player.getSummons()) {
			if (summon instanceof Rift) {
				Rift rift = (Rift) summon;
				if (rift.countDown() == 0) {
					context.getLogic().markAsDestroyed(rift);
				}
			}
		}
		CardCollection dumb = new CardCollection();
		for (Card card : player.getHand()) {
			card.setLocation(CardLocation.HAND);
			if (card.hasAttribute(Attribute.ONE_TURN)) {
				dumb.add(card);
			}
		}
		for (Card card : dumb) {
			removeCard(player.getId(), card);
		}
		for (Iterator<CardCostModifier> iterator = context.getCardCostModifiers().iterator(); iterator.hasNext();) {
			CardCostModifier cardCostModifier = iterator.next();
			if (cardCostModifier.isExpired()) {
				iterator.remove();
			}
		}
		checkForDeadEntities();
	}

	public void equipWeapon(int playerId, Weapon weapon) {
		equipWeapon(playerId, weapon, false);
	}

	public void equipWeapon(int playerId, Weapon weapon, boolean battlecry) {
		Player player = context.getPlayer(playerId);

		weapon.setId(idFactory.generateId());
		Weapon currentWeapon = player.getHero().getWeapon();
		
		if (currentWeapon != null) {
			player.getSetAsideZone().add(currentWeapon);
		}

		log("{} equips weapon {}", player.getHero(), weapon);
		player.getHero().setWeapon(weapon);

		if (battlecry && weapon.getBattlecry() != null) {
			resolveBattlecry(playerId, weapon);
		}
		
		if (currentWeapon != null) {
			log("{} discards currently equipped weapon {}", player.getHero(), currentWeapon);
			destroy(currentWeapon);
			player.getSetAsideZone().remove(currentWeapon);
		}
		
		player.getStatistics().equipWeapon(weapon);
		weapon.onEquip(context, player);
		weapon.setActive(context.getActivePlayerId() == playerId);
		if (weapon.hasSpellTrigger()) {
			for (SpellTrigger spellTrigger : weapon.getSpellTriggers()) {
				addGameEventListener(player, spellTrigger, weapon);
			}
		}
		if (weapon.getCardCostModifier() != null) {
			addManaModifier(player, weapon.getCardCostModifier(), weapon);
		}
		checkForDeadEntities();
		context.fireGameEvent(new WeaponEquippedEvent(context, weapon));
		context.fireGameEvent(new BoardChangedEvent(context));
	}

	public void fight(Player player, Actor attacker, Actor defender) {
		log("{} attacks {}", attacker, defender);

		context.getEnvironment().put(Environment.ATTACKER_REFERENCE, attacker.getReference());

		TargetAcquisitionEvent targetAcquisitionEvent = new TargetAcquisitionEvent(context, player.getId(), ActionType.PHYSICAL_ATTACK, attacker, defender);
		context.fireGameEvent(targetAcquisitionEvent);
		Actor target = defender;
		if (context.getEnvironment().containsKey(Environment.TARGET_OVERRIDE)) {
			target = (Actor) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
		}
		context.getEnvironment().remove(Environment.TARGET_OVERRIDE);

		// if (attacker.hasTag(GameTag.FUMBLE) && randomBool()) {
		// log("{} fumbled and hits another target", attacker);
		// target = getAnotherRandomTarget(player, attacker, defender,
		// EntityReference.ENEMY_CHARACTERS);
		// }

		if (target != defender) {
			log("Target of attack was changed! New Target: {}", target);
		}

		if (attacker.hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING)) {
			applyAttribute(attacker, Attribute.IMMUNE);
		}
		int attackerDamage;
		attackerDamage = attacker.getAttack();
		removeAttribute(attacker, Attribute.STEALTH);
		if (attacker.getEntityType().equals(EntityType.HERO)) {
			Hero hero = (Hero) attacker;
			if (hero.getWeapon() != null) {
				if (!hero.getWeapon().isActive()) {
					hero.activateWeapon(true);
					attackerDamage = attacker.getAttack();
					hero.activateWeapon(false);
				}

			}
		}
		int defenderDamage = target.getAttack();

		context.fireGameEvent(new PhysicalAttackEvent(context, attacker, target, attackerDamage));
		// secret may have killed attacker ADDENDUM: or defender
		if (attacker.isDestroyed() || target.isDestroyed()) {
			context.getEnvironment().remove(Environment.ATTACKER_REFERENCE);
			return;
		}

		if (target.getOwner() == -1) {
			logger.error("Target has no owner!! {}", target);
		}

		Player owningPlayer = context.getPlayer(target.getOwner());
		boolean damaged = damage(owningPlayer, target, attackerDamage, attacker) > 0;
		if (defenderDamage > 0) {
			damage(player, attacker, defenderDamage, target);
		}
		if (attacker.hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING)) {
			attacker.removeAttribute(Attribute.IMMUNE);
		}

		if (attacker.getEntityType() == EntityType.HERO) {
			Hero hero = (Hero) attacker;
			Weapon weapon = hero.getWeapon();
			if (weapon != null && weapon.isActive()) {
				modifyDurability(hero.getWeapon(), hasAttribute(context.getPlayer(hero.getOwner()), Attribute.NO_DURABILITY) ? 0 : -1);
				if (weapon.hasAttribute(Attribute.POISONOUS) && attackerDamage > 0) {
					destroy(defender);
				}
				if (weapon.hasAttribute(Attribute.LIFESTEAL) && attackerDamage > 0) {
					heal(player, hero, attackerDamage, (Actor) weapon);
				}
			}
		}
		attacker.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, -1);

		context.fireGameEvent(new AfterPhysicalAttackEvent(context, attacker, target, damaged ? attackerDamage : 0));

		context.getEnvironment().remove(Environment.ATTACKER_REFERENCE);
		checkForDeadEntities();
	}

	public void gainArmor(Player player, int armor) {
		logger.debug("{} gains {} armor", player.getHero(), armor);
		player.getHero().modifyArmor(armor);
		if (armor > 0) {
			player.getStatistics().armorGained(armor);
			context.fireGameEvent(new ArmorGainedEvent(context, player.getHero(), armor));
		}
	}

	public String generateCardID() {
		return TEMP_CARD_LABEL + idFactory.generateId();
	}

	public Actor getAnotherRandomTarget(Player player, Actor attacker, Actor originalTarget, EntityReference potentialTargets) {
		List<Entity> validTargets = context.resolveTarget(player, null, potentialTargets);
		// cannot redirect to attacker
		validTargets.remove(attacker);
		// cannot redirect to original target
		validTargets.remove(originalTarget);
		if (validTargets.isEmpty()) {
			return originalTarget;
		}

		return (Actor) SpellUtils.getRandomTarget(validTargets);
	}

	/**
	 * Returns the first value of the attribute encountered. This method should
	 * be used with caution, as the result is random if there are different
	 * values of the same attribute in play.
	 * 
	 * @param player
	 * @param attr
	 *            Which attribute to find
	 * @param defaultValue
	 *            The value returned if no occurrence of the attribute is found
	 * @return the first occurrence of the value of attribute or defaultValue
	 */
	public int getAttributeValue(Player player, Attribute attr, int defaultValue) {
		for (Summon summon : player.getSummons()) {
			if (summon.hasAttribute(attr)) {
				return summon.getAttributeValue(attr);
			}
		}

		return defaultValue;
	}

	public GameAction getAutoHeroPowerAction(int playerId) {
		return actionLogic.getAutoHeroPower(context, context.getPlayer(playerId));
	}

	/**
	 * Return the greatest value of the attribute from all Actors of a Player.
	 * This method will return infinite if an Attribute value is negative, so
	 * use this method with caution.
	 * 
	 * @param player
	 *            Which Player to check
	 * @param attr
	 *            Which attribute to find
	 * @return The highest value from all sources. -1 is considered infinite.
	 */
	public int getGreatestAttributeValue(Player player, Attribute attr) {
		int greatest = Math.max(INFINITE, player.getHero().getAttributeValue(attr));
		if (greatest == INFINITE) {
			return greatest;
		}
		for (Summon summon : player.getSummons()) {
			if (summon.hasAttribute(attr)) {
				if (summon.getAttributeValue(attr) > greatest) {
					greatest = summon.getAttributeValue(attr);
				}
				if (summon.getAttributeValue(attr) == INFINITE) {
					return INFINITE;
				}
			}
		}
		return greatest;
	}

	public MatchResult getMatchResult(Player player, Player opponent) {
		boolean playerLost = hasPlayerLost(player);
		boolean opponentLost = hasPlayerLost(opponent);
		if (playerLost && opponentLost) {
			return MatchResult.DOUBLE_LOSS;
		} else if (playerLost || opponentLost) {
			return MatchResult.WON;
		}
		return MatchResult.RUNNING;
	}

	public int getModifiedManaCost(Player player, Card card) {
		int manaCost = card.getBaseManaCost();
		List<CardCostModifier> costModifiers = context.getCardCostModifiers();
		List<CardCostModifier> minCostModifiers = new ArrayList<>();
		List<CardCostModifier> setCostModifiers = new ArrayList<>();
		List<CardCostModifier> restCostModifiers = new ArrayList<>();
		costModifiers.forEach(modifier -> {
			if (modifier.getMinValue() != 0) {
				minCostModifiers.add(modifier);
			} else if (modifier.hasOperation()) {
				if (modifier.getOperation().equals(AlgebraicOperation.SET)) {
					setCostModifiers.add(modifier);
				}
			} else restCostModifiers.add(modifier);
		});
		costModifiers = new ArrayList<>();
		costModifiers.addAll(minCostModifiers);
		costModifiers.addAll(setCostModifiers);
		costModifiers.addAll(restCostModifiers);
		for (CardCostModifier costModifier : costModifiers) {
			if (!costModifier.appliesTo(card, context)) {
				continue;
			}
			int minValue = 0;
			manaCost = costModifier.process(card, manaCost);
			if (costModifier.getMinValue() > minValue) {
				minValue = costModifier.getMinValue();
			}
			manaCost = MathUtils.clamp(manaCost, minValue, Integer.MAX_VALUE);
		}
		if (card.getManaCostModifier() != null) {
			manaCost -= card.getManaCostModifier().getValue(context, player, null, card);
		}
		if (card.hasAttribute(Attribute.MANA_COST_MODIFIER)) {
			manaCost += card.getAttributeValue(Attribute.MANA_COST_MODIFIER);
		}

		manaCost = MathUtils.clamp(manaCost, 0, Integer.MAX_VALUE);
		return manaCost;
	}

	public List<IGameEventListener> getQuests(Player player) {
		List<IGameEventListener> quests = context.getTriggersAssociatedWith(player.getHero().getReference());
		for (Iterator<IGameEventListener> iterator = quests.iterator(); iterator.hasNext();) {
			IGameEventListener trigger = iterator.next();
			if (!(trigger instanceof Quest)) {
				iterator.remove();
			}
		}
		return quests;
	}

	public List<IGameEventListener> getSecrets(Player player) {
		List<IGameEventListener> secrets = context.getTriggersAssociatedWith(player.getHero().getReference());
		for (Iterator<IGameEventListener> iterator = secrets.iterator(); iterator.hasNext();) {
			IGameEventListener trigger = iterator.next();
			if (!(trigger instanceof Secret)) {
				iterator.remove();
			}
		}
		return secrets;
	}

	public int getTotalAttributeValue(Attribute attr) {
		int total = 0;
		for (Player player : context.getPlayers()) {
			total += getTotalAttributeValue(player, attr);
		}
		return total;
	}

	public int getTotalAttributeValue(Player player, Attribute attr) {
		int total = player.getHero().getAttributeValue(attr);
		for (Summon summon : player.getSummons()) {
			if (!summon.hasAttribute(attr)) {
				continue;
			}

			total += summon.getAttributeValue(attr);
		}
		return total;
	}

	public int getTotalAttributeMultiplier(Player player, Attribute attribute) {
		int total = 1;
		if (player.getHero().hasAttribute(attribute)) {
			player.getHero().getAttributeValue(attribute);
		}
		for (Summon summon : player.getSummons()) {
			if (summon.hasAttribute(attribute)) {
				total *= summon.getAttributeValue(attribute);
			}
		}
		return total;
	}

	public List<GameAction> getValidActions(int playerId) {
		Player player = context.getPlayer(playerId);
		return actionLogic.getValidActions(context, player);
	}

	public List<Entity> getValidTargets(int playerId, GameAction action) {
		Player player = context.getPlayer(playerId);
		return targetLogic.getValidTargets(context, player, action);
	}

	public Player getWinner(Player player, Player opponent) {
		boolean playerLost = hasPlayerLost(player);
		boolean opponentLost = hasPlayerLost(opponent);
		if (playerLost && opponentLost) {
			return null;
		} else if (opponentLost) {
			return player;
		} else if (playerLost) {
			return opponent;
		}
		return null;
	}

	private void handleEnrage(Actor entity) {
		if (!entity.hasAttribute(Attribute.ENRAGABLE)) {
			return;
		}
		boolean enraged = entity.getHp() < entity.getMaxHp();
		// enrage state has not changed; do nothing
		if (entity.hasAttribute(Attribute.ENRAGED) == enraged) {
			return;
		}

		if (enraged) {
			log("{} is now enraged", entity);
			entity.setAttribute(Attribute.ENRAGED);
		} else {
			log("{} is no longer enraged", entity);
			entity.removeAttribute(Attribute.ENRAGED);
		}

		context.fireGameEvent(new EnrageChangedEvent(context, entity));
	}

	private void handleFrozen(Actor actor) {
		if (!actor.hasAttribute(Attribute.FROZEN)) {
			return;
		}
		if (actor.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) >= actor.getMaxNumberOfAttacks()) {
			removeAttribute(actor, Attribute.FROZEN);
		}
	}

	public boolean hasAttribute(Player player, Attribute attr) {
		if (player.getHero().hasAttribute(attr)) {
			return true;
		}
		if (player.hasAttribute(attr)) {
			return true;
		}
		for (Summon summon : player.getSummons()) {
			if (summon.hasAttribute(attr) && !summon.hasAttribute(Attribute.PENDING_DESTROY)) {
				return true;
			}
		}

		return false;
	}

	public List<Object> getAttributes(Player player, Attribute attr) {
		List<Object> results = new ArrayList<>();
		if (player.getHero().hasAttribute(attr)) {
			results.add(player.getHero().getAttribute(attr));
		}

		if (player.hasAttribute(attr)) {
			results.add(player.getAttribute(attr));
		}
		for (Summon summon : player.getSummons()) {
			if (summon.hasAttribute(attr) && !summon.hasAttribute(Attribute.PENDING_DESTROY)) {
				results.add(summon.getAttribute(attr));
			}
		}
		return results;
	}

	public boolean hasAutoHeroPower(int player) {
		return actionLogic.hasAutoHeroPower(context, context.getPlayer(player));
	}

	public boolean hasCard(Player player, Card card) {
		for (Card heldCard : player.getHand()) {
			if (card.getCardId().equals(heldCard.getCardId())) {
				return true;
			}
		}
		if (player.getHero().getHeroPower().getCardId().equals(card.getCardId())) {
			return true;
		}
		return false;
	}

	public void heal(Player player, Actor target, int healing, Entity source) {
		if (hasAttribute(player, Attribute.INVERT_HEALING)) {
			log("All healing inverted, deal damage instead!");
			damage(player, target, healing, source);
			return;
		}
		if (source != null && source instanceof Card
				&& (((Card) source).getCardType().isCardType(CardType.SPELL)
				|| ((Card) source).getCardType().isCardType(CardType.HERO_POWER))) {
			healing = applyAmplify(player, healing, Attribute.HEAL_AMPLIFY_MULTIPLIER);
		}
		boolean success = false;
		switch (target.getEntityType()) {
		case MINION:
			success = healMinion((Actor) target, healing);
			break;
		case HERO:
			success = healHero((Hero) target, healing);
			break;
		default:
			break;
		}
		target.setAttribute(Attribute.LAST_HEAL, healing);
		if (success) {
			HealEvent healEvent = new HealEvent(context, player.getId(), target, healing);
			context.fireGameEvent(healEvent);
			player.getStatistics().heal(healing);
		}
	}

	private boolean healHero(Hero hero, int healing) {
		int newHp = Math.min(hero.getMaxHp(), hero.getHp() + healing);
		int oldHp = hero.getHp();
		if (logger.isDebugEnabled()) {
			log(hero + " is healed for " + healing + ", hp now: " + newHp / hero.getMaxHp());
		}

		hero.setHp(newHp);
		hero.setAttribute(Attribute.CUSTOM_7);
		return newHp != oldHp;
	}

	private boolean healMinion(Actor minion, int healing) {
		int newHp = Math.min(minion.getMaxHp(), minion.getHp() + healing);
		int oldHp = minion.getHp();
		if (logger.isDebugEnabled()) {
			log(minion + " is healed for " + healing + ", hp now: " + newHp + "/" + minion.getMaxHp());
		}

		minion.setHp(newHp);
		handleEnrage(minion);
		return newHp != oldHp;
	}

	public void init(int playerId, boolean begins) {
		Player player = context.getPlayer(playerId);
		player.getHero().setId(idFactory.generateId());
		player.getHero().setOwner(playerId);
		player.getHero().setMaxHp(player.getHero().getAttributeValue(Attribute.BASE_HP));
		player.getHero().setHp(player.getHero().getAttributeValue(Attribute.BASE_HP));

		player.getHero().getHeroPower().setId(idFactory.generateId());
		assignCardIds(player.getDeck());
		assignCardIds(player.getHand());

		log("Setting hero hp to {} for {}", player.getHero().getHp(), player.getName());

		player.getDeck().shuffle();

		mulligan(player, begins);

		for (Card card : player.getDeck()) {
			if (card.getAttribute(Attribute.DECK_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.DECK_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
		}
		for (Card card : player.getHand()) {
			if (card.getAttribute(Attribute.DECK_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.DECK_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
		}

		GameStartEvent gameStartEvent = new GameStartEvent(context, player.getId());
		context.fireGameEvent(gameStartEvent);
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public JoustEvent joust(Player player, CardType cardType) {
		Card ownCard = player.getDeck().getRandomOfType(cardType);
		Card opponentCard = null;
		boolean won = false;
		// no minions left in deck - automatically loose joust
		if (ownCard == null) {
			won = false;
			log("Jousting LOST - no minion card left");
		} else {
			Player opponent = context.getOpponent(player);
			opponentCard = opponent.getDeck().getRandomOfType(cardType);
			// opponent has no minions left in deck - automatically win joust
			if (opponentCard == null) {
				won = true;
				log("Jousting WON - opponent has no minion card left");
			} else {
				// both players have minion cards left, the initiator needs to
				// have the one with
				// higher mana cost to win the joust
				won = ownCard.getBaseManaCost() > opponentCard.getBaseManaCost();

				log("Jousting {} - {} vs. {}", won ? "WON" : "LOST", ownCard, opponentCard);
			}
		}
		JoustEvent joustEvent = new JoustEvent(context, player.getId(), won, ownCard, opponentCard);
		context.fireGameEvent(joustEvent);
		return joustEvent;
	}

	private void log(String message) {
		logToDebugHistory(message);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}

	private void log(String message, Object param1) {
		logToDebugHistory(message, param1);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message, param1);
		}
	}

	private void log(String message, Object param1, Object param2) {
		logToDebugHistory(message, param1, param2);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message, param1, param2);
		}
	}

	private void log(String message, Object param1, Object param2, Object param3) {
		logToDebugHistory(message, param1, param2, param3);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message, param1, param2, param3);
		}
	}

	private void logToDebugHistory(String message, Object... params) {
		if (!BuildConfig.DEV_BUILD) {
			return;
		}
		if (debugHistory.size() == MAX_HISTORY_ENTRIES) {
			debugHistory.poll();
		}
		if (params != null && params.length > 0) {
			message = message.replaceAll("\\{\\}", "%s");
			message = String.format(message, params);
		}

		debugHistory.add(message);
	}

	public void markAsDestroyed(Actor target) {
		if (target != null) {
			target.setAttribute(Attribute.DESTROYED);
		}
	}

	public void mindControl(Player player, Summon summon) {
		log("{} mind controls {}", player.getName(), summon);
		Player opponent = context.getOpponent(player);
		if (!opponent.getSummons().contains(summon)) {
			// logger.warn("Minion {} cannot be mind-controlled, because
			// opponent does not own it.", minion);
			return;
		}
		if (canSummonMoreMinions(player)) {
			context.getOpponent(player).getSummons().remove(summon);
			player.getSummons().add(summon);
			summon.setOwner(player.getId());
			applyAttribute(summon, Attribute.SUMMONING_SICKNESS);
			refreshAttacksPerRound(summon);
			List<IGameEventListener> triggers = context.getTriggersAssociatedWith(summon.getReference());
			removeSpellTriggers(summon);
			for (IGameEventListener trigger : triggers) {
				addGameEventListener(player, trigger, summon);
			}
			context.fireGameEvent(new BoardChangedEvent(context));
		} else {
			markAsDestroyed(summon);
		}
	}

	public void modifyCurrentMana(int playerId, int mana) {
		Player player = context.getPlayer(playerId);
		int newMana = Math.min(player.getMana() + mana, MAX_MANA);
		player.setMana(newMana);
	}

	public void modifyDurability(Weapon weapon, int durability) {
		log("Durability of weapon {} is changed by {}", weapon, durability);

		weapon.modifyAttribute(Attribute.HP, durability);
		if (durability > 0) {
			weapon.modifyAttribute(Attribute.MAX_HP, durability);
		}
	}

	public void modifyMaxHp(Actor actor, int value) {
		actor.setMaxHp(value);
		actor.setHp(value);
		handleEnrage(actor);
	}

	public void modifyMaxMana(Player player, int delta) {
		log("Maximum mana was changed by {} for {}", delta, player.getName());
		int maxMana = MathUtils.clamp(player.getMaxMana() + delta, 0, GameLogic.MAX_MANA);
		player.setMaxMana(maxMana);
		if (delta < 0 && player.getMana() > player.getMaxMana()) {
			modifyCurrentMana(player.getId(), delta);
		}
	}

	private void mulligan(Player player, boolean begins) {
		int numberOfStarterCards = begins ? STARTER_CARDS : STARTER_CARDS + 1;
		List<Card> starterCards = new ArrayList<>();

		int number2 = numberOfStarterCards;
		Card yupCard = null;
		
		for (Card maybeCard : player.getDeck()) {
			if (maybeCard.getCardId().contains("quest_"))
			{
				number2 -= 1;
				yupCard = maybeCard;
				starterCards.add(maybeCard);
				break;
			}
		}
		
		player.getDeck().remove(yupCard);
		
		
		for (int j = 0; j < number2; j++) {
			Card randomCard = player.getDeck().getRandom();
			if (randomCard != null) {
				player.getDeck().remove(randomCard);
				log("Player {} been offered card {} for mulligan", player.getName(), randomCard);
				starterCards.add(randomCard);
			}
		}

		List<Card> discardedCards = player.getBehaviour().mulligan(context, player, starterCards);

		// remove player selected cards from starter cards
		for (Card discardedCard : discardedCards) {
			log("Player {} mulligans {} ", player.getName(), discardedCard);
			starterCards.remove(discardedCard);
		}

		// draw random cards from deck until required starter card count is
		// reached
		while (starterCards.size() < numberOfStarterCards) {
			Card randomCard = player.getDeck().getRandom();
			player.getDeck().remove(randomCard);
			starterCards.add(randomCard);
		}

		// put the mulligan cards back in the deck
		for (Card discardedCard : discardedCards) {
			player.getDeck().add(discardedCard);
		}

		for (Card starterCard : starterCards) {
			if (starterCard != null) {
				receiveCard(player.getId(), starterCard);
			}
		}

		// second player gets the coin additionally
		if (!begins) {
			Card theCoin = CardCatalogue.getCardById("spell_the_coin");
			receiveCard(player.getId(), theCoin);
		}
	}

	public void panicDump() {
		logger.error("=========PANIC DUMP=========");
		for (String entry : debugHistory) {
			logger.error(entry);
		}
	}

	public void performGameAction(int playerId, GameAction action) {
		try {
			debugHistory.add(action.toString());
			if (playerId != context.getActivePlayerId()) {
                logger.warn("Player {} tries to perform an action, but it is not his turn!", context.getPlayer(playerId).getName());
            }
			if (action.getTargetRequirement() != TargetSelection.NONE) {
                Entity target = context.resolveSingleTarget(action.getTargetKey());
                if (target != null) {
                    context.getEnvironment().put(Environment.TARGET, target.getReference());
                } else {
                    context.getEnvironment().put(Environment.TARGET, null);
                }
            }

			action.execute(context, playerId);

			context.getEnvironment().remove(Environment.TARGET);
			if (action.getActionType() != ActionType.BATTLECRY) {
                checkForDeadEntities();
            }
		} catch (NullPointerException e){
		}
	}

	public void playCard(int playerId, CardReference cardReference) {
		Player player = context.getPlayer(playerId);
		Card card = context.resolveCardReference(cardReference);
		card.setName(card.getDesc().name);
		card.removeAttribute(Attribute.ONE_TURN);
		int modifiedManaCost = getModifiedManaCost(player, card);
		if (hasAttribute(player, Attribute.CARD_TYPE_COSTS_HEALTH)) {
			List<Object> results = getAttributes(player, Attribute.CARD_TYPE_COSTS_HEALTH);
			List<CardType> cardTypes = new ArrayList<>();
			for (Object object : results) {
				CardType cardType = (CardType) object;
				cardTypes.add(cardType);
			}
			if (cardTypes.contains(card.getCardType())) {
				context.getEnvironment().put(Environment.LAST_MANA_COST, 0);
				damage(player, player.getHero(), modifiedManaCost, card, true);
			}
		} else if (((card.getRace() == Race.MURLOC || card.getRace() == Race.ALL)) && player.hasAttribute(Attribute.MURLOCS_COST_HEALTH)) {
			context.getEnvironment().put(Environment.LAST_MANA_COST, 0);
			damage(player, player.getHero(), modifiedManaCost, card, true);
		} else if (card.hasAttribute(Attribute.COSTS_HEALTH)) {
			context.getEnvironment().put(Environment.LAST_MANA_COST, 0);
			damage(player, player.getHero(), modifiedManaCost, card, true);
		}
		
		else {
			context.getEnvironment().put(Environment.LAST_MANA_COST, modifiedManaCost);
			modifyCurrentMana(playerId, -modifiedManaCost);
			player.getStatistics().manaSpent(modifiedManaCost);
		}
		log("{} plays {}", player.getName(), card);

		player.getStatistics().cardPlayed(card, context.getTurn());
		player.lastCardPlayed = card;
		CardPlayedEvent cardPlayedEvent = new CardPlayedEvent(context, playerId, card);
		context.fireGameEvent(cardPlayedEvent);

		if (card.hasAttribute(Attribute.OVERLOAD)) {
			context.fireGameEvent(new OverloadEvent(context, playerId, card, card.getAttributeValue(Attribute.OVERLOAD)));
		}

		removeCard(playerId, card);

		if ((card.getCardType().isCardType(CardType.SPELL))) {
			GameEvent spellCastedEvent = new SpellCastedEvent(context, playerId, card);
			context.fireGameEvent(spellCastedEvent);
			if (card.hasAttribute(Attribute.COUNTERED)) {
				log("{} was countered!", card.getName());
				return;
			}
		}

		if (card.hasAttribute(Attribute.OVERLOAD)) {
			player.modifyAttribute(Attribute.OVERLOAD, card.getAttributeValue(Attribute.OVERLOAD));
		}
	}

	public void playQuest(Player player, Quest quest) {
		playQuest(player, quest, true);
	}

	public void playQuest(Player player, Quest quest, boolean fromHand) {
		log("{} has a new quest activated: {}", player.getName(), quest.getSource());
		addGameEventListener(player, quest, player.getHero());
		player.getSecrets().add(quest.getSource().getCardId());
		player.getQuests().add(quest.getSource().getCardId());
		if (fromHand) {
			context.fireGameEvent(new QuestPlayedEvent(context, player.getId(), (QuestCard) quest.getSource()));
		}
	}

	public void playSecret(Player player, Secret secret) {
		playSecret(player, secret, false);
	}

	public void playSecret(Player player, Secret secret, boolean fromHand) {
		log("{} has a new secret activated: {}", player.getName(), secret.getSource());
		addGameEventListener(player, secret, player.getHero());
		player.getSecrets().add(secret.getSource().getCardId());
		
		if (fromHand) {
			context.fireGameEvent(new SecretPlayedEvent(context, player.getId(), (SecretCard) secret.getSource()));
		}
	}

	public void processTargetModifiers(Player player, GameAction action) {
		HeroPower heroPower = player.getHero().getHeroPower();
		List<String> okPowers = new ArrayList<String>();
		okPowers.add(CardCatalogue.getCardById("hero_power_steady_shot").getCardId());
		okPowers.add(CardCatalogue.getCardById("hero_power_ballista_shot").getCardId());
		okPowers.add(CardCatalogue.getCardById("hero_power_life_tap").getCardId());
		if (!okPowers.contains(heroPower.getCardId())) {
			return;
		}
		if (action.getActionType() == ActionType.HERO_POWER && hasAttribute(player, Attribute.HERO_POWER_CAN_TARGET_MINIONS)) {
			PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
			SpellDesc targetChangedSpell = spellCardAction.getSpell().removeArg(SpellArg.TARGET);
			spellCardAction.setSpell(targetChangedSpell);
			spellCardAction.setTargetRequirement(TargetSelection.ENEMY_CHARACTERS);
		}
	}

	/**
	 * 
	 * @param max
	 *            Upper bound of random number (exclusive)
	 * @return Random number between 0 and max (exclusive)
	 */
	public int random(int max) {
		return ThreadLocalRandom.current().nextInt(max);
	}

	public boolean randomBool() {
		return ThreadLocalRandom.current().nextBoolean();
	}

	public void receiveCard(int playerId, Card card) {
		receiveCard(playerId, card, null);
	}

	public void receiveCard(int playerId, Card card, Entity source) {
		receiveCard(playerId, card, source, false);
	}

	public void receiveCard(int playerId, Card card, Entity source, boolean drawn) {
		Player player = context.getPlayer(playerId);
		if (card.getId() == IdFactory.UNASSIGNED) {
			card.setId(idFactory.generateId());
		}

		card.setOwner(playerId);
		CardCollection hand = player.getHand();

		if (hand.getCount() < MAX_HAND_CARDS) {
			if (card.getAttribute(Attribute.PASSIVE_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.PASSIVE_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
			
			log("{} receives card {}", player.getName(), card);
			hand.add(card);
			card.setLocation(CardLocation.HAND);
			CardType sourceType = null;
			if (source instanceof Card) {
				Card sourceCard = (Card) source;
				sourceType = sourceCard.getCardType();
			}
			context.fireGameEvent(new DrawCardEvent(context, playerId, card, sourceType, drawn));
		} else {
			log("{} has too many cards on his hand, card destroyed: {}", player.getName(), card);
			if (drawn) {
				context.fireGameEvent(new CardRevealedEvent(context, playerId, context.getCardById("burn_card"), 0));
				context.fireGameEvent(new CardRevealedEvent(context, playerId, card, 1.5));
			}
			discardCard(player, card);
		}
	}

	public void refreshAttacksPerRound(Entity entity) {
		int attacks = 1;
		if (entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			attacks = MEGA_WINDFURY_ATTACKS;
		} else if (entity.hasAttribute(Attribute.WINDFURY)) {
			attacks = WINDFURY_ATTACKS;
		}
		entity.setAttribute(Attribute.NUMBER_OF_ATTACKS, attacks);
	}

	public void removeAttribute(Entity entity, Attribute attr) {
		if (!entity.hasAttribute(attr)) {
			return;
		}
		if (attr == Attribute.MEGA_WINDFURY && entity.hasAttribute(Attribute.WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, WINDFURY_ATTACKS - MEGA_WINDFURY_ATTACKS);
		}
		if (attr == Attribute.WINDFURY && !entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, 1 - WINDFURY_ATTACKS);
		} else if (attr == Attribute.MEGA_WINDFURY) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, 1 - MEGA_WINDFURY_ATTACKS);
		}
		entity.removeAttribute(attr);
		context.fireGameEvent(new AttributeLostEvent(context, entity, attr));
		log("Removing attribute {} from {}", attr, entity);
	}

	public void removeCard(int playerId, Card card) {
		Player player = context.getPlayer(playerId);
		log("Card {} has been moved from the HAND to the GRAVEYARD", card);
		card.setLocation(CardLocation.GRAVEYARD);
		removeSpellTriggers(card);
		player.getHand().remove(card);
		player.getGraveyard().add(card);
	}

	public void removeAllCards(int playerId) {
		for (Card card : context.getPlayer(playerId).getHand().toList()) {
			removeCard(playerId, card);
		}
	}

	public void removeCardFromDeck(int playerID, Card card) {
		Player player = context.getPlayer(playerID);
		log("Card {} has been moved from the DECK to the GRAVEYARD", card);
		card.setLocation(CardLocation.GRAVEYARD);
		removeSpellTriggers(card);
		player.getDeck().remove(card);
		player.getGraveyard().add(card);
	}

	public void removeQuests(Player player) {
		log("All quests for {} have been destroyed", player.getName());
		// This actually works amazingly
		for (IGameEventListener quest : getQuests(player)) {
			quest.onRemove(context);
			context.removeTrigger(quest);
		}
		player.getSecrets().removeAll(player.getQuests());
		player.getQuests().clear();
	}

	public void removeSummon(Summon summon, boolean peacefully) {
		removeSpellTriggers(summon);

		log("{} was removed", summon);

		summon.setAttribute(Attribute.DESTROYED);

		Player owner = context.getPlayer(summon.getOwner());
		owner.getSummons().remove(summon);
		if (peacefully) {
			owner.getSetAsideZone().add(summon);
		} else {
			owner.getGraveyard().add(summon);
		}
		context.fireGameEvent(new BoardChangedEvent(context));
	}

	public void removeSecrets(Player player) {
		log("All secrets for {} have been destroyed", player.getName());
		// this only works while Secrets are the only SpellTrigger on the heroes
		// Web - Lol, it works now.
		for (IGameEventListener secret : getSecrets(player)) {
			secret.onRemove(context);
			context.removeTrigger(secret);
		}
		player.getSecrets().clear();
		player.getSecrets().addAll(player.getQuests());
	}

	private void removeSpellTriggers(Entity entity) {
		removeSpellTriggers(entity, true);
	}

	public void removeSpellTriggers(Entity entity, boolean removeAuras) {
		EntityReference entityReference = entity.getReference();
		for (IGameEventListener trigger : context.getTriggersAssociatedWith(entityReference)) {
			if (!removeAuras && trigger instanceof Aura) {
				continue;
			}
			log("SpellTrigger {} was removed for {}", trigger, entity);
			trigger.onRemove(context);
		}
		context.removeTriggersAssociatedWith(entityReference, removeAuras);
		for (Iterator<CardCostModifier> iterator = context.getCardCostModifiers().iterator(); iterator.hasNext();) {
			CardCostModifier cardCostModifier = iterator.next();
			if (cardCostModifier.getHostReference().equals(entityReference)) {
				iterator.remove();
			}
		}
	}

	public void replaceCard(int playerId, Card oldCard, Card newCard) {
		Player player = context.getPlayer(playerId);
		if (newCard.getId() == IdFactory.UNASSIGNED) {
			newCard.setId(idFactory.generateId());
		}
		
		if (!player.getHand().contains(oldCard)) {
			return;
		}

		newCard.setOwner(playerId);
		CardCollection hand = player.getHand();

		if (newCard.getAttribute(Attribute.PASSIVE_TRIGGER) != null) {
			TriggerDesc triggerDesc = (TriggerDesc) newCard.getAttribute(Attribute.PASSIVE_TRIGGER);
			addGameEventListener(player, triggerDesc.create(), newCard);
		}

		log("{} replaces card {} with card {}", player.getName(), oldCard, newCard);
		hand.replace(oldCard, newCard);
		removeCard(playerId, oldCard);
		newCard.setLocation(CardLocation.HAND);
		context.fireGameEvent(new DrawCardEvent(context, playerId, newCard, null, false));
	}
	
	public void replaceCardInDeck(int playerId, Card oldCard, Card newCard) {
		Player player = context.getPlayer(playerId);
		if (newCard.getId() == IdFactory.UNASSIGNED) {
			newCard.setId(idFactory.generateId());
		}
		
		if (!player.getDeck().contains(oldCard)) {
			return;
		}

		newCard.setOwner(playerId);
		CardCollection deck = player.getDeck();

		if (newCard.getAttribute(Attribute.DECK_TRIGGER) != null) {
			TriggerDesc triggerDesc = (TriggerDesc) newCard.getAttribute(Attribute.DECK_TRIGGER);
			addGameEventListener(player, triggerDesc.create(), newCard);
		}

		log("{} replaces card {} with card {}", player.getName(), oldCard, newCard);
		deck.replace(oldCard, newCard);
		removeCardFromDeck(playerId, oldCard);
		newCard.setLocation(CardLocation.DECK);
	}

	private void resolveBattlecry(int playerId, Actor actor) {
		BattlecryAction battlecry = actor.getBattlecry();
		Player player = context.getPlayer(playerId);
		if (!battlecry.canBeExecuted(context, player)) {
			return;
		}

		GameAction battlecryAction = null;
		battlecry.setSource(actor.getReference());
		if (battlecry.getTargetRequirement() != TargetSelection.NONE) {
			List<Entity> validTargets = targetLogic.getValidTargets(context, player, battlecry);
			if (validTargets.isEmpty()) {
				return;
			}

			List<GameAction> battlecryActions = new ArrayList<>();
			for (Entity validTarget : validTargets) {
				GameAction targetedBattlecry = battlecry.clone();
				targetedBattlecry.setTarget(validTarget);
				battlecryActions.add(targetedBattlecry);
			}
			
			if (attributeExists(Attribute.ALL_RANDOM_FINAL_DESTINATION)) {
				battlecryAction = battlecryActions.get(random(battlecryActions.size()));
			} else {
				battlecryAction = player.getBehaviour().requestAction(context, player, battlecryActions);
			}
		} else {
			battlecryAction = battlecry;
		}
		if (hasAttribute(player, Attribute.DOUBLE_BATTLECRIES) && actor.getSourceCard().hasAttribute(Attribute.BATTLECRY)) {
			// You need DOUBLE_BATTLECRIES before your battlecry action, not after.
			performGameAction(playerId, battlecryAction);
			if (!battlecry.canBeExecuted(context, player)) {
				return;
			}
			performGameAction(playerId, battlecryAction);
		} else {
			performGameAction(playerId, battlecryAction);
		}
	}

	public void resolveDeathrattles(Player player, Actor actor) {
		resolveDeathrattles(player, actor, -1);
	}

	public void resolveDeathrattles(Player player, Actor actor, int boardPosition) {
		if (!actor.hasAttribute(Attribute.DEATHRATTLES)) {
			return;
		}
		if (boardPosition == -1) {
			player.getSummons().indexOf(actor);
		}
		boolean doubleDeathrattles = hasAttribute(player, Attribute.DOUBLE_DEATHRATTLES);
		EntityReference sourceReference = actor.getReference();
		for (SpellDesc deathrattleTemplate : actor.getDeathrattles()) {
			SpellDesc deathrattle = deathrattleTemplate.addArg(SpellArg.BOARD_POSITION_ABSOLUTE, boardPosition);
			castSpell(player.getId(), deathrattle, sourceReference, EntityReference.NONE, false);
			if (doubleDeathrattles) {
				castSpell(player.getId(), deathrattle, sourceReference, EntityReference.NONE, false);
			}
		}
	}

	public void questTriggered(Player player, Quest quest) {
		log("Quest was trigged: {}", quest.getSource());
		player.getSecrets().remove(quest.getSource().getCardId());
		player.getQuests().remove(quest.getSource().getCardId());
		context.fireGameEvent(new QuestSuccessfulEvent(context, (QuestCard) quest.getSource(), player.getId()));
	}

	public void secretTriggered(Player player, Secret secret) {
		log("Secret was trigged: {}", secret.getSource());
		player.getSecrets().remove(secret.getSource().getCardId());
		context.fireGameEvent(new SecretRevealedEvent(context, (SecretCard) secret.getSource(), player.getId()));
		Card card = secret.getSource().clone();
		card.removeAttribute(Attribute.SECRET);
		context.fireGameEvent(new CardRevealedEvent(context, player.getId(), card, 1.2));
		context.fireGameEvent(new CardRevealedEvent(context, context.getOpponent(player).getId(), card, 1.2));
	}

	// TODO: circular dependency. Very ugly, refactor!
	public void setContext(GameContext context) {
		this.context = context;
	}

	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	public void shuffleToDeck(Player player, Card card) {
		if (card.getId() == IdFactory.UNASSIGNED) {
			card.setId(idFactory.generateId());
		}
		card.setLocation(CardLocation.DECK);

		if (player.getDeck().getCount() < MAX_DECK_SIZE) {
			player.getDeck().addRandomly(card);
			
			if (card.getAttribute(Attribute.DECK_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.DECK_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
			log("Card {} has been shuffled to {}'s deck", card, player.getName());
		}
	}

	public void silence(int playerId, Minion target) {
		context.fireGameEvent(new SilenceEvent(context, playerId, target));
		final HashSet<Attribute> immuneToSilence = new HashSet<Attribute>();
		immuneToSilence.add(Attribute.HP);
		immuneToSilence.add(Attribute.MAX_HP);
		immuneToSilence.add(Attribute.BASE_HP);
		immuneToSilence.add(Attribute.BASE_ATTACK);
		immuneToSilence.add(Attribute.SUMMONING_SICKNESS);
		immuneToSilence.add(Attribute.AURA_ATTACK_BONUS);
		immuneToSilence.add(Attribute.AURA_HP_BONUS);
		immuneToSilence.add(Attribute.AURA_UNTARGETABLE_BY_SPELLS);
		immuneToSilence.add(Attribute.RACE);
		immuneToSilence.add(Attribute.NUMBER_OF_ATTACKS);

		List<Attribute> tags = new ArrayList<Attribute>();
		tags.addAll(target.getAttributes().keySet());
		for (Attribute attr : tags) {
			if (immuneToSilence.contains(attr)) {
				continue;
			}
			removeAttribute(target, attr);
		}
		removeSpellTriggers(target);

		int oldMaxHp = target.getMaxHp();
		target.setMaxHp(target.getAttributeValue(Attribute.BASE_HP));
		target.setAttack(target.getAttributeValue(Attribute.BASE_ATTACK));
		if (target.getHp() > target.getMaxHp()) {
			target.setHp(target.getMaxHp());
		} else if (oldMaxHp < target.getMaxHp()) {
			target.setHp(target.getHp() + target.getMaxHp() - oldMaxHp);
		}
		target.setAttribute(Attribute.SILENCED, true);
		log("{} was silenced", target);
	}

	public void startTurn(int playerId) {
		Player player = context.getPlayer(playerId);
		if (player.getMaxMana() < MAX_MANA) {
			player.setMaxMana(player.getMaxMana() + 1);
		}
		player.getStatistics().startTurn();

		player.setLockedMana(player.getAttributeValue(Attribute.OVERLOAD));
		int mana = Math.min(player.getMaxMana() - player.getLockedMana(), MAX_MANA);
		player.setMana(mana);
		String manaString = player.getMana() + "/" + player.getMaxMana();
		if (player.getLockedMana() > 0) {
			manaString += " (" + player.getLockedMana() + " locked by overload)";
		}
		log("{} starts his turn with {} mana", player.getName(), manaString);

		player.removeAttribute(Attribute.OVERLOAD);
		for (Summon summon : player.getSummons()) {
			summon.removeAttribute(Attribute.TEMPORARY_ATTACK_BONUS);
		}

		player.getHero().getHeroPower().setUsed(0);
		player.getHero().activateWeapon(true);
		refreshAttacksPerRound(player.getHero());
		for (Summon summon : player.getSummons()) {
			summon.removeAttribute(Attribute.SUMMONING_SICKNESS);
			refreshAttacksPerRound(summon);
		}
		context.fireGameEvent(new TurnStartEvent(context, player.getId()));
		drawCard(playerId, null);
		checkForDeadEntities();
	}

	public boolean summon(int playerId, Summon summon) {
		return summon(playerId, summon, null, -1, false);
	}

	public boolean summon(int playerId, Summon summon, Card source, int index, boolean resolveBattlecry) {
		Player player = context.getPlayer(playerId);
		if (!canSummonMoreMinions(player)) {
			log("{} cannot summon any more summons, {} is destroyed", player.getName(), summon);
			return false;
		}
		summon.setId(idFactory.generateId());
		summon.setOwner(player.getId());

		context.getSummonReferenceStack().push(summon.getReference());


		log("{} summons {}", player.getName(), summon);

		if (index < 0 || index >= player.getSummons().size()) {
			player.getSummons().add(summon);
		} else {
			player.getSummons().add(index, summon);
		}
		if (summon instanceof Minion) {
			Minion minion = (Minion) summon;

			context.fireGameEvent(new BeforeSummonEvent(context, minion, source));
		}
		context.fireGameEvent(new BoardChangedEvent(context));

		if (resolveBattlecry && summon.getBattlecry() != null) {
			resolveBattlecry(player.getId(), summon);
			checkForDeadEntities();
		}

		if (context.getEnvironment().get(Environment.TRANSFORM_REFERENCE) != null) {
			summon = (Summon) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TRANSFORM_REFERENCE));
			summon.setBattlecry(null);
			context.getEnvironment().remove(Environment.TRANSFORM_REFERENCE);
		}

		context.fireGameEvent(new BoardChangedEvent(context));

		if (summon instanceof Minion) {
			Minion minion = (Minion) summon;
			player.getStatistics().minionSummoned(minion, context.getTurn());

			if (context.getEnvironment().get(Environment.TARGET_OVERRIDE) != null) {
				Actor actor = (Actor) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
				context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
				SummonEvent summonEvent = new SummonEvent(context, actor, source, minion.getAttack() + minion.getHp());

				context.fireGameEvent(summonEvent);
			} else {
				SummonEvent summonEvent = new SummonEvent(context, minion, source, minion.getAttack() + minion.getHp());
				context.fireGameEvent(summonEvent);
			}

			applyAttribute(minion, Attribute.SUMMONING_SICKNESS);
			refreshAttacksPerRound(minion);
		} else if (summon instanceof Permanent) {
			Permanent permanent = (Permanent) summon;
			player.getStatistics().permanentSummoned(permanent, context.getTurn());
			
		}

		if (summon.hasSpellTrigger()) {
			for (SpellTrigger trigger : summon.getSpellTriggers()) {
				addGameEventListener(player, trigger, summon);
			}
		}

		if (summon.getCardCostModifier() != null) {
			addManaModifier(player, summon.getCardCostModifier(), summon);
		}

		if (summon instanceof Minion) {
			Minion minion = (Minion) summon;
			if (source != null) {
				source.setAttribute(Attribute.ATTACK, source.getAttributeValue(Attribute.BASE_ATTACK));
				source.setAttribute(Attribute.ATTACK_BONUS, 0);
				source.setAttribute(Attribute.MAX_HP, source.getAttributeValue(Attribute.BASE_HP));
				source.setAttribute(Attribute.HP, source.getAttributeValue(Attribute.BASE_HP));
				source.setAttribute(Attribute.HP_BONUS, 0);
			}
			handleEnrage(minion);

			context.getSummonReferenceStack().pop();
			if (player.getSummons().contains(minion)) {
				context.fireGameEvent(new MinionExistEvent(context, minion, source));
				context.fireGameEvent(new AfterSummonEvent(context, minion, source));
			}
		}
		context.fireGameEvent(new BoardChangedEvent(context));

		return true;
	}

	public void transformMinion (Summon summon, Summon newSummon) {
		transformMinion(summon, newSummon, false);
	}

	public void transformMinion(Summon summon, Summon newSummon, boolean quiet) {
		// Remove any spell triggers associated with the old minion.
		removeSpellTriggers(summon);

		Player owner = context.getPlayer(summon.getOwner());
		int index = owner.getSummons().indexOf(summon);
		owner.getSummons().remove(summon);
		
		// If we want to straight up remove a minion from existence without
		// killing it, this would be the best way.
		if (newSummon != null) {
			log("{} was transformed to {}", summon, newSummon);

			// Give the new minion an ID.
			newSummon.setId(idFactory.generateId());
			newSummon.setOwner(owner.getId());
	
			// If the minion being transforms is being summoned, replace the old
			// minion on the stack.
			// Otherwise, summon the add the new minion.
			// However, do not give a summon event.
			if (!context.getSummonReferenceStack().isEmpty() && context.getSummonReferenceStack().peek().equals(summon.getReference())
					&& !context.getEnvironment().containsKey(Environment.TRANSFORM_REFERENCE)) {
				context.getEnvironment().put(Environment.TRANSFORM_REFERENCE, newSummon.getReference());
				owner.getSummons().add(index, newSummon);
	
				// It's quite possible that this is actually supposed to add the
				// minion to the zone it was originally in.
				// This means minions in the SetAsideZone or the Graveyard that are
				// targeted (through bizarre mechanics)
				// add the minion to there. This will be tested eventually with
				// Resurrect, Recombobulator, and Illidan.
				// Since this is unknown, this is the patch for it.
			} else if (!owner.getSetAsideZone().contains(summon)) {
				if (index < 0 || index >= owner.getSummons().size()) {
					owner.getSummons().add(newSummon);
				} else {
					owner.getSummons().add(index, newSummon);
				}




				applyAttribute(newSummon, Attribute.SUMMONING_SICKNESS);
				refreshAttacksPerRound(newSummon);
	
				if (newSummon.hasSpellTrigger()) {
					for (SpellTrigger spellTrigger : newSummon.getSpellTriggers()) {
						addGameEventListener(owner, spellTrigger, newSummon);
					}
				}
				if (!quiet) {
					context.fireGameEvent(new MinionExistEvent(context, newSummon, newSummon.getSourceCard()));
				}
				if (newSummon.getCardCostModifier() != null) {
					addManaModifier(owner, newSummon.getCardCostModifier(), newSummon);
				}
	
				handleEnrage(newSummon);
			} else {
				owner.getSetAsideZone().add(newSummon);
				newSummon.setId(idFactory.generateId());
				newSummon.setOwner(owner.getId());
				removeSpellTriggers(newSummon);
				return;
			}
		
		}

		// Move the old minion to the Set Aside Zone
		owner.getSetAsideZone().add(summon);

		context.fireGameEvent(new BoardChangedEvent(context));
	}

	public void useHeroPower(int playerId) {
		Player player = context.getPlayer(playerId);
		HeroPower power = player.getHero().getHeroPower();
		int modifiedManaCost = getModifiedManaCost(player, power);
		modifyCurrentMana(playerId, -modifiedManaCost);
		log("{} uses {}", player.getName(), power);
		power.markUsed();
		player.getStatistics().cardPlayed(power, context.getTurn());
		context.fireGameEvent(new HeroPowerUsedEvent(context, playerId, power));
	}
	
	public void info (String string) {
		logger.info(string);
	}

}
