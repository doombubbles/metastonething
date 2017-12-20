package net.demilich.metastone.game.behaviour.human;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.IActionSelectionListener;

import java.io.Serializable;

public class HumanTargetOptions implements Serializable {

	private final IActionSelectionListener actionSelectionListener;
	private final GameContext context;
	private final int playerId;
	private final ActionGroup actionGroup;
	private final boolean multiplayer;

	public HumanTargetOptions(IActionSelectionListener actionSelectionListener, GameContext context, int playerId, ActionGroup actionGroup, boolean multiplayer) {
		this.actionSelectionListener = actionSelectionListener;
		this.context = context;
		this.playerId = playerId;
		this.actionGroup = actionGroup;
		this.multiplayer = multiplayer;
	}

	public ActionGroup getActionGroup() {
		return actionGroup;
	}

	public IActionSelectionListener getActionSelectionListener() {
		return actionSelectionListener;
	}

	public GameContext getContext() {
		return context;
	}

	public int getPlayerId() {
		return playerId;
	}

	public boolean isMultiplayer() {
		return multiplayer;
	}

}
