package net.demilich.metastone.game.behaviour.human;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;

public class HumanActionOptions implements Serializable {

	private final HumanBehaviour behaviour;
	private final GameContext context;
	private Player player;
	private Player opponent;
	private final List<GameAction> validActions;

	public HumanActionOptions(HumanBehaviour behaviour, GameContext context, Player player, List<GameAction> validActions, Player opponent) {
		this.behaviour = behaviour;
		this.context = context;
		this.player = player;
		this.validActions = validActions;
	}

	public HumanBehaviour getBehaviour() {
		return behaviour;
	}

	public GameContext getContext() {
		return context;
	}

	public Player getPlayer() {
		return player;
	}

	public HumanActionOptions switchPlayers() {
		Player temp = player;
		player = opponent;
		opponent = temp;
		return this;
	}

	public List<GameAction> getValidActions() {
		return validActions;
	}
	
	public boolean matchesExistingGroup(GameAction action, Collection<ActionGroup> existingActionGroups) {
		for (ActionGroup actionGroup : existingActionGroups) {
			if (actionGroup.getPrototype().isSameActionGroup(action)) {
				actionGroup.add(action);
				return true;
			}
		}
		return false;
	}

}
