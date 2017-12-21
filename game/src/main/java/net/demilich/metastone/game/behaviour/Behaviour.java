package net.demilich.metastone.game.behaviour;

import net.demilich.metastone.game.GameContext;

import java.io.Serializable;

public abstract class Behaviour implements IBehaviour, Serializable {

	public IBehaviour clone() {
		try {
			return (IBehaviour) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
	}

}
