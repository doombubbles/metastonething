package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.events.TurnEndEvent;
import net.demilich.metastone.game.targeting.TargetSelection;

public class ExtraTurnAction extends GameAction {

	public ExtraTurnAction() {
		setActionType(ActionType.EXTRA_TURN);
		setTargetRequirement(TargetSelection.NONE);
	}

	@Override
	public void execute(GameContext context, int playerId) {
		context.getPlayer(playerId).modifyAttribute(Attribute.EXTRA_TURNS, -1);
		context.fireGameEvent(new TurnEndEvent(context, playerId));
		context.getLogic().startTurn(playerId);
	}

	@Override
	public String getPromptText() {
		return "[Extra turn]";
	}

	@Override
	public boolean isSameActionGroup(GameAction anotherAction) {
		return anotherAction.getActionType() == getActionType();
	}

	@Override
	public String toString() {
		return String.format("[%s]", getActionType());
	}

}
