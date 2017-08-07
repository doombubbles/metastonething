package net.demilich.metastone.game.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.EndTurnAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.IChooseOneCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.heroes.powers.HeroPower;
import net.demilich.metastone.game.targeting.CardLocation;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class ActionLogic {

	private final TargetLogic targetLogic = new TargetLogic();

	public GameAction getAutoHeroPower(GameContext context, Player player) {
		return getHeroPowerActions(context, player).get(0);
	}

	private List<GameAction> getHeroAttackActions(GameContext context, Player player) {
		List<GameAction> heroAttackActions = new ArrayList<GameAction>();
		Hero hero = player.getHero();
		if (!hero.canAttackThisTurn()) {
			return heroAttackActions;
		}
		rollout(hero, context, player, heroAttackActions);

		return heroAttackActions;
	}

	

	private List<GameAction> getHeroPowerActions(GameContext context, Player player) {
		List<GameAction> heroPowerActions = new ArrayList<GameAction>();
		HeroPower heroPower = player.getHero().getHeroPower();
		heroPower.onWillUse(context, player);
		CardReference heroPowerReference = new CardReference(player.getId(), CardLocation.HERO_POWER, heroPower.getId(),
				heroPower.getName());
		if (!context.getLogic().canPlayCard(player.getId(), heroPowerReference)) {
			return heroPowerActions;
		}
		if (heroPower.hasAttribute(Attribute.CHOOSE_ONE)) {
			IChooseOneCard chooseOneCard = (IChooseOneCard) heroPower;
			if (context.getLogic().hasAttribute(player, Attribute.BOTH_CHOOSE_ONE_OPTIONS)) {
				rollout(chooseOneCard.playBothOptions(), context, player, heroPowerActions);
			} else {
				for (GameAction chooseOneAction : chooseOneCard.playOptions()) {
					rollout(chooseOneAction, context, player, heroPowerActions);
				}
			}
			
		} else {
			rollout(heroPower, context, player, heroPowerActions);
		}

		return heroPowerActions;
	}

	private List<GameAction> getPhysicalAttackActions(GameContext context, Player player) {
		List<GameAction> physicalAttackActions = new ArrayList<GameAction>();
		physicalAttackActions.addAll(getHeroAttackActions(context, player));

		for (Minion minion : player.getMinions()) {
			if (!minion.canAttackThisTurn()) {
				continue;
			}

			rollout(minion, context, player, physicalAttackActions);
		}
		return physicalAttackActions;
	}

	private List<GameAction> getPlayCardActions(GameContext context, Player player) {
		List<GameAction> playCardActions = new ArrayList<GameAction>();
		playCardActions.addAll(getHeroPowerActions(context, player));

		for (Card card : player.getHand()) {
			CardReference cardReference = new CardReference(player.getId(), CardLocation.HAND, card.getId(), card.getName());
			if (!context.getLogic().canPlayCard(player.getId(), cardReference)) {
				continue;
			}

			if (card.hasAttribute(Attribute.CHOOSE_ONE)) {
				IChooseOneCard chooseOneCard = (IChooseOneCard) card;
				if (context.getLogic().hasAttribute(player, Attribute.BOTH_CHOOSE_ONE_OPTIONS) && chooseOneCard.hasBothOptions()) {
					GameAction chooseOneAction = chooseOneCard.playBothOptions();
					rollout(chooseOneAction, context, player, playCardActions);
				} else {
					for (GameAction chooseOneAction : chooseOneCard.playOptions()) {
						rollout(chooseOneAction, context, player, playCardActions);
					}
				}
			} else {
				rollout(card, context, player, playCardActions);
			}

		}
		return playCardActions;
	}

	public List<GameAction> getValidActions(GameContext context, Player player) {
		List<GameAction> validActions = new ArrayList<GameAction>();
		validActions.addAll(getPhysicalAttackActions(context, player));
		validActions.addAll(getPlayCardActions(context, player));
		if (context.getTurnState() != TurnState.TURN_ENDED) {
			validActions.add(new EndTurnAction());
		}

		return validActions;
	}

	public boolean hasAutoHeroPower(GameContext context, Player player) {
		HeroPower heroPower = player.getHero().getHeroPower();
		heroPower.onWillUse(context, player);
		CardReference heroPowerReference = new CardReference(player.getId(), CardLocation.HERO_POWER, heroPower.getId(),
				heroPower.getName());
		return (context.getLogic().canPlayCard(player.getId(), heroPowerReference) && heroPower.getTargetRequirement() == TargetSelection.AUTO);
	}

	public void rollout(GameAction action, GameContext context, Player player, Collection<GameAction> actions) {
		context.getLogic().processTargetModifiers(player, action);
		if (action.getTargetRequirement() == TargetSelection.NONE || action.getTargetRequirement() == TargetSelection.AUTO) {
			actions.add(action);
		} else {
			for (Entity validTarget : targetLogic.getValidTargets(context, player, action)) {
				GameAction rolledOutAction = action.clone();
				rolledOutAction.setTarget(validTarget);
				actions.add(rolledOutAction);
			}
		}
	}
	
	public void rollout(Minion minion, GameContext context, Player player, Collection<GameAction> actions) {
		GameAction action = new PhysicalAttackAction(minion.getReference());
		action.setSource(minion.getReference());
		if (action.getTargetRequirement() == TargetSelection.NONE || action.getTargetRequirement() == TargetSelection.AUTO) {
			actions.add(action);
		} else {
			for (Entity validTarget : targetLogic.getValidTargets(context, player, action)) {
				GameAction rolledOutAction = action.clone();
				rolledOutAction.setTarget(validTarget);
				actions.add(rolledOutAction);
			}
		}
	}
	
	public void rollout(Card card, GameContext context, Player player, Collection<GameAction> actions) {
		GameAction action = card.play();
		action.setSource(card.getReference());
		context.getLogic().processTargetModifiers(player, action);
		if (action.getTargetRequirement() == TargetSelection.NONE || action.getTargetRequirement() == TargetSelection.AUTO) {
			actions.add(action);
		} else {
			for (Entity validTarget : targetLogic.getValidTargets(context, player, action)) {
				GameAction rolledOutAction = action.clone();
				rolledOutAction.setTarget(validTarget);
				actions.add(rolledOutAction);
			}
		}
	}
	private void rollout(Hero hero, GameContext context, Player player, List<GameAction> heroAttackActions) {
		GameAction action = new PhysicalAttackAction(hero.getReference());
		action.setSource(hero.getReference());
		context.getLogic().processTargetModifiers(player, action);
		if (action.getTargetRequirement() == TargetSelection.NONE || action.getTargetRequirement() == TargetSelection.AUTO) {
			heroAttackActions.add(action);
		} else {
			for (Entity validTarget : targetLogic.getValidTargets(context, player, action)) {
				GameAction rolledOutAction = action.clone();
				rolledOutAction.setTarget(validTarget);
				heroAttackActions.add(rolledOutAction);
			}
		}
	}
}
