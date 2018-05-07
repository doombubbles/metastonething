package net.demilich.metastone.game.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Rift;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class TargetLogic implements Serializable {

	private static Logger logger = LoggerFactory.getLogger(TargetLogic.class);

	private static List<Entity> singleTargetAsList(Entity target) {
		ArrayList<Entity> list = new ArrayList<>(1);
		list.add(target);
		return list;
	}

	private boolean containsTaunters(List<Minion> minions) {
		for (Entity entity : minions) {
			if (entity.hasAttribute(Attribute.TAUNT) && !entity.hasAttribute(Attribute.STEALTH) && !entity.hasAttribute(Attribute.IMMUNE)) {
				return true;
			}
		}
		return false;
	}

	private List<Entity> filterTargets(GameContext context, Player player, GameAction action, List<Entity> potentialTargets) {
		List<Entity> validTargets = new ArrayList<>();
		for (Entity entity : potentialTargets) {
			// special case for 'SYSTEM' action, which are used in Sandbox Mode
			// we do not want to restrict those actions by STEALTH or
			// UNTARGETABLE_BY_SPELLS
			if (action.getActionType() == ActionType.SYSTEM && action.canBeExecutedOn(context, player, entity)) {
				validTargets.add(entity);
				continue;
			}
			if ((action.getActionType() == ActionType.SPELL || action.getActionType() == ActionType.HERO_POWER)
					&& (entity.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)
					|| (entity.getEntityType().equals(EntityType.HERO) && context.getLogic().hasAttribute(context.getPlayer(entity.getOwner()), Attribute.ELUSIVE_HERO))
					|| (entity.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)))) {
				continue;
			}

			if (entity.getOwner() != player.getId() && (entity.hasAttribute(Attribute.STEALTH) || entity.hasAttribute(Attribute.IMMUNE))) {
				continue;
			}
			if (entity.getOwner() != player.getId() && entity instanceof Hero && context.getLogic().hasAttribute(context.getPlayer(entity.getOwner()), Attribute.IMMUNE_HERO)) {
				continue;
			}

			if (action.canBeExecutedOn(context, player, entity)) {
				validTargets.add(entity);
			}
		}
		return validTargets;
	}

	public Entity findEntity(GameContext context, EntityReference targetKey) {
		int targetId = targetKey.getId();
		Entity environmentResult = findInEnvironment(context, targetKey);
		if (environmentResult != null) {
			return environmentResult;
		}
		for (Player player : context.getPlayers()) {
			if (player.getId() == targetId) {
				return player;
			}
			if (player.getHero().getId() == targetId) {
				return player.getHero();
			} else if (player.getHero().getWeapon() != null && player.getHero().getWeapon().getId() == targetId) {
				return player.getHero().getWeapon();
			} 

			for (Summon summon : player.getSummons()) {
				if (summon.getId() == targetId) {
					return summon;
				}
			}

			for (Entity entity : player.getGraveyard()) {
				if (entity.getId() == targetId) {
					return entity;
				}
			}
			for (Entity entity : player.getSetAsideZone()) {
				if (entity.getId() == targetId) {
					return entity;
				}
			}
		}

		Entity cardResult = findInCards(context.getPlayer1(), targetId);
		if (cardResult == null) {
			cardResult = findInCards(context.getPlayer2(), targetId);
		}
		if (cardResult != null) {
			return cardResult;
		}
		logger.error("Id " + targetId + " not found!");
		logger.error(context.toString());
		logger.error(context.getEnvironment().toString());
		throw new RuntimeException("Target not found exception: " + targetKey);
	}

	private Entity findInCards(Player player, int targetId) {
		if (player.getHero().getHeroPower().getId() == targetId) {
			return player.getHero().getHeroPower();
		}
		if (player.getHero().hasHeroPower2()) {
			if (player.getHero().getHeroPower2().getId() == targetId) {
				return player.getHero().getHeroPower2();
			}
		}
		for (Card card : player.getHand()) {
			if (card.getId() == targetId) {
				return card;
			}
		}
		for (Card card : player.getDeck()) {
			if (card.getId() == targetId) {
				return card;
			}
		}

		return null;
	}

	private Entity findInEnvironment(GameContext context, EntityReference targetKey) {
		if (!context.getEventTargetStack().isEmpty() && targetKey == EntityReference.EVENT_TARGET) {
			return context.resolveSingleTarget(context.getEventTargetStack().peek());
		}
		if (!context.getEventSourceStack().isEmpty() && targetKey == EntityReference.EVENT_SOURCE) {
			return context.resolveSingleTarget(context.getEventSourceStack().peek());
		}
		return null;
	}

	private List<Entity> getEntities(GameContext context, Player player, TargetSelection targetRequirement) {
		Player opponent = context.getOpponent(player);
		List<Entity> entities = new ArrayList<>();
		if (targetRequirement == TargetSelection.ENEMY_HERO || targetRequirement == TargetSelection.ENEMY_CHARACTERS
				|| targetRequirement == TargetSelection.ANY || targetRequirement == TargetSelection.HEROES || targetRequirement == TargetSelection.MINIONS_AND_ENEMY_HERO) {
			entities.add(opponent.getHero());
		}
		if (targetRequirement == TargetSelection.ENEMY_MINIONS || targetRequirement == TargetSelection.ENEMY_CHARACTERS
				|| targetRequirement == TargetSelection.MINIONS || targetRequirement == TargetSelection.ANY
				|| targetRequirement == TargetSelection.MINIONS_AND_ENEMY_HERO || targetRequirement == TargetSelection.MINIONS_AND_FRIENDLY_HERO) {
			entities.addAll(opponent.getMinions());
		}
		if (targetRequirement == TargetSelection.FRIENDLY_HERO || targetRequirement == TargetSelection.FRIENDLY_CHARACTERS
				|| targetRequirement == TargetSelection.ANY || targetRequirement == TargetSelection.HEROES
				|| targetRequirement == TargetSelection.MINIONS_AND_FRIENDLY_HERO) {
			entities.add(player.getHero());
		}
		if (targetRequirement == TargetSelection.FRIENDLY_MINIONS || targetRequirement == TargetSelection.FRIENDLY_CHARACTERS
				|| targetRequirement == TargetSelection.MINIONS || targetRequirement == TargetSelection.ANY
				|| targetRequirement == TargetSelection.MINIONS_AND_ENEMY_HERO || targetRequirement == TargetSelection.MINIONS_AND_FRIENDLY_HERO) {
			entities.addAll(player.getMinions());
		}
		List<Entity> destroyedEntities = new ArrayList<Entity>();
		for (Entity entity : entities) {
			if (entity != null && entity.hasAttribute(Attribute.PENDING_DESTROY)) {
				destroyedEntities.add(entity);
			}
		}
		entities.removeAll(destroyedEntities);
		return entities;
	}

	private List<Entity> getTaunters(List<Minion> entities) {
		List<Entity> taunters = new ArrayList<>();
		for (Actor entity : entities) {
			if (entity.hasAttribute(Attribute.TAUNT) && !entity.hasAttribute(Attribute.STEALTH) && !entity.hasAttribute(Attribute.IMMUNE)) {
				taunters.add(entity);
			}
		}
		return taunters;
	}

	public List<Entity> getValidTargets(GameContext context, Player player, GameAction action) {
		TargetSelection targetRequirement = action.getTargetRequirement();
		ActionType actionType = action.getActionType();
		Player opponent = context.getOpponent(player);

		// if there is a minion with TAUNT and the action is of type physical
		// attack only allow corresponding minions as targets
		if (actionType == ActionType.PHYSICAL_ATTACK
				&& (targetRequirement == TargetSelection.ENEMY_CHARACTERS || targetRequirement == TargetSelection.ENEMY_MINIONS)
				&& containsTaunters(opponent.getMinions())) {
			return getTaunters(opponent.getMinions());
		}
		if (actionType == ActionType.SUMMON) {
			// you can summon next to any friendly minion or provide no target
			// (=null)
			// in which case the minion will appear to the very right of your
			// board
			List<Entity> summonTargets = getEntities(context, player, targetRequirement);
			summonTargets.add(null);
			return summonTargets;
		}
		List<Entity> potentialTargets = getEntities(context, player, targetRequirement);
		return filterTargets(context, player, action, potentialTargets);
	}

	public List<Entity> resolveTargetKey(GameContext context, Player player, Entity source, EntityReference targetKey) {
		if (targetKey == null || targetKey == EntityReference.NONE) {
			return null;
		}
		Player opponent = context.getOpponent(player);
		if (targetKey.getId() == EntityReference.ALL_CHARACTERS.getId()) {
			return getEntities(context, player, TargetSelection.ANY);
		} else if (targetKey.getId() == EntityReference.ALL_MINIONS.getId()) {
			return getEntities(context, player, TargetSelection.MINIONS);
		} else if (targetKey.getId() == EntityReference.ENEMY_CHARACTERS.getId()) {
			return getEntities(context, player, TargetSelection.ENEMY_CHARACTERS);
		} else if (targetKey.getId() == EntityReference.ENEMY_HERO.getId()) {
			return getEntities(context, player, TargetSelection.ENEMY_HERO);
		} else if (targetKey.getId() == EntityReference.ENEMY_MINIONS.getId()) {
			return getEntities(context, player, TargetSelection.ENEMY_MINIONS);
		} else if (targetKey.getId() == EntityReference.FRIENDLY_CHARACTERS.getId()) {
			return getEntities(context, player, TargetSelection.FRIENDLY_CHARACTERS);
		} else if (targetKey.getId() == EntityReference.FRIENDLY_HERO.getId()) {
			return getEntities(context, player, TargetSelection.FRIENDLY_HERO);
		} else if (targetKey.getId() == EntityReference.FRIENDLY_MINIONS.getId()) {
			return getEntities(context, player, TargetSelection.FRIENDLY_MINIONS);
		} else if (targetKey.getId() == EntityReference.OTHER_FRIENDLY_MINIONS.getId()) {
			List<Entity> targets = getEntities(context, player, TargetSelection.FRIENDLY_MINIONS);
			targets.remove(source);
			return targets;
		} else if (targetKey.getId() == EntityReference.ALL_OTHER_CHARACTERS.getId()) {
			List<Entity> targets = getEntities(context, player, TargetSelection.ANY);
			targets.remove(source);
			return targets;
		} else if (targetKey.getId() == EntityReference.ALL_OTHER_MINIONS.getId()) {
			List<Entity> targets = getEntities(context, player, TargetSelection.MINIONS);
			targets.remove(source);
			return targets;
		} else if (targetKey.getId() == EntityReference.ALL_OTHER_ENEMIES.getId()) {
			List<Entity> targets = getEntities(context, player, TargetSelection.ENEMY_CHARACTERS);
			targets.remove(source);
			return targets;
		} else if (targetKey.getId() == EntityReference.ADJACENT_MINIONS.getId()) {
			return new ArrayList<>(context.getAdjacentSummons(player, source.getReference()));
		} else if (targetKey.getId() == EntityReference.OPPOSITE_MINIONS.getId()) {
			return new ArrayList<>(context.getOppositeSummons(player, source.getReference()));
		} else if (targetKey.getId() == EntityReference.MINIONS_TO_LEFT.getId()) {
			return new ArrayList<>(context.getLeftSummons(player, source.getReference()));
		} else if (targetKey.getId() == EntityReference.MINIONS_TO_RIGHT.getId()) {
			return new ArrayList<>(context.getRightSummons(player, source.getReference()));
		} else if (targetKey.getId() == EntityReference.SELF.getId()) {
			return singleTargetAsList(source);
		} else if (targetKey.getId() == EntityReference.EVENT_TARGET.getId()) {
			if (context.getEventTargetStack().empty()) {
				return null;
			}
			return singleTargetAsList(context.resolveSingleTarget(context.getEventTargetStack().peek()));
		} else if (targetKey.getId() == EntityReference.TARGET.getId()) {
			return singleTargetAsList(context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET)));
		} else if (targetKey.getId() == EntityReference.SPELL_TARGET.getId()) {
			return singleTargetAsList(context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.SPELL_TARGET)));
		} else if (targetKey.getId() == EntityReference.KILLED_MINION.getId()) {
			return singleTargetAsList(context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.KILLED_MINION)));
		} else if (targetKey.getId() == EntityReference.ATTACKER_REFERENCE.getId()) {
			return singleTargetAsList(context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.ATTACKER_REFERENCE)));
		} else if (targetKey.getId() == EntityReference.PENDING_CARD.getId()) {
			return singleTargetAsList((Entity) context.getPendingCard());
		} else if (targetKey.getId() == EntityReference.EVENT_CARD.getId()) {
			return singleTargetAsList((Entity) context.getEventCard());
		} else if (targetKey.getId() == EntityReference.FRIENDLY_WEAPON.getId()) {
			if (player.getHero().getWeapon() != null) {
				return singleTargetAsList(player.getHero().getWeapon());
			} else {
				return new ArrayList<>();
			}
		} else if (targetKey.getId() == EntityReference.ENEMY_WEAPON.getId()) {
			if (opponent.getHero().getWeapon() != null) {
				return singleTargetAsList(opponent.getHero().getWeapon());
			} else {
				return new ArrayList<>();
			}
		} else if (targetKey.getId() == EntityReference.FRIENDLY_HAND.getId()) {
			return new ArrayList<>(player.getHand().toList());
		} else if (targetKey.getId() == EntityReference.FRIENDLY_DECK.getId()) {
			return new ArrayList<>(player.getDeck().toList());
		} else if (targetKey.getId() == EntityReference.ENEMY_HAND.getId()) {
			return new ArrayList<>(context.getOpponent(player).getHand().toList());
		} else if (targetKey.getId() == EntityReference.ENEMY_DECK.getId()) {
			return new ArrayList<>(context.getOpponent(player).getDeck().toList());
		} else if (targetKey.getId() == EntityReference.FRIENDLY_PLAYER.getId()) {
			return singleTargetAsList(player);
		} else if (targetKey.getId() == EntityReference.ENEMY_PLAYER.getId()) {
			return singleTargetAsList(context.getOpponent(player));
		} else if (targetKey.getId() == EntityReference.OTHER_ENEMY_MINIONS.getId()) {
			List<Entity> targets = getEntities(context, player, TargetSelection.ENEMY_MINIONS);
			targets.remove(source);
			if (!context.getEventTargetStack().empty()) {
				targets.remove(context.resolveSingleTarget(context.getEventTargetStack().peek()));
			}
			return targets;
		} else if (targetKey.getId() == EntityReference.RIGHTMOST_ENEMY_MINION.getId()) {
			List<Minion> minions =  opponent.getMinions();
			return singleTargetAsList(minions.get(minions.size() - 1));
		} else if (targetKey.getId() == EntityReference.RIGHTMOST_FRIENDLY_MINION.getId()) {
			List<Minion> minions =  player.getMinions();
			return singleTargetAsList(minions.get(minions.size() - 1));
		} else if (targetKey.getId() == EntityReference.LEFTMOST_ENEMY_MINION.getId()) {
			List<Minion> minions =  opponent.getMinions();
			return singleTargetAsList(minions.get(0));
		} else if (targetKey.getId() == EntityReference.LEFTMOST_FRIENDLY_MINION.getId()) {
			List<Minion> minions =  player.getMinions();
			return singleTargetAsList(minions.get(0));
		} else if (targetKey.getId() == EntityReference.EVENT_SOURCE.getId()) {
			if (context.getEventSourceStack().empty()) {
				return null;
			}
			EntityReference target = context.getEventSourceStack().peek();
			return singleTargetAsList(context.resolveSingleTarget(target));
		} else if (targetKey.getId() == EntityReference.OUTPUT.getId()) {
			if (context.getOutputStack().empty()) {
				return null;
			}
			EntityReference output = context.getOutputStack().peek();
			return singleTargetAsList(context.resolveSingleTarget(output));
		} else if (targetKey.getId() == EntityReference.FRIENDLY_RIFTS.getId()) {
			List<Entity> rifts = new ArrayList<>();
			for (Summon summon : player.getSummons()) {
				if (summon instanceof Rift) {
					rifts.add(summon);
				}
			}
			return rifts;
		} else if (targetKey.getId() == EntityReference.ENEMY_RIFTS.getId()) {
			List<Entity> rifts = new ArrayList<>();
			for (Summon summon : context.getOpponent(player).getSummons()) {
				if (summon instanceof Rift) {
					rifts.add(summon);
				}
			}
			return rifts;
		} else if (targetKey.getId() == EntityReference.FRIENDLY_TOP_CARD.getId()) {
			if (player.getDeck().isEmpty()) {
				return null;
			}
			return singleTargetAsList(player.getDeck().get(0));
		} else if (targetKey.getId() == EntityReference.ENEMY_TOP_CARD.getId()) {
			if (opponent.getDeck().isEmpty()) {
				return null;
			}
			return singleTargetAsList(opponent.getDeck().get(0));
		}
		return singleTargetAsList(findEntity(context, targetKey));
	}

}
