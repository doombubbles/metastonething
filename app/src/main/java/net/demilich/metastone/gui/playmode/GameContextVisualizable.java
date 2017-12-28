package net.demilich.metastone.gui.playmode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.Server;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.gui.multiplayermode.Client;

public class GameContextVisualizable extends GameContext {

	private final List<GameEvent> gameEvents = new ArrayList<>();

	private boolean blockedByAnimation;

	private boolean multiplayer;

	private boolean exceptions = false;

	public GameContextVisualizable(Player player1, Player player2, GameLogic logic, DeckFormat deckFormat, boolean multiplayer) {
		super(player1, player2, logic, deckFormat);
		this.multiplayer = multiplayer;
	}

	public boolean isMultiplayer() {
		return multiplayer;
	}

	protected boolean acceptAction(GameAction nextAction) {
		if (!ignoreEvents()) {
			return true;
		}
		while (ignoreEvents()) {
			try {
				Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
			} catch (InterruptedException e) {
			}
		}
		return false;
	}

	@Override
	public void fireGameEvent(GameEvent gameEvent) {
		if (ignoreEvents()) {
			return;
		}
		super.fireGameEvent(gameEvent);
		getGameEvents().add(gameEvent);
	}

	public synchronized List<GameEvent> getGameEvents() {
		return gameEvents;
	}

	public boolean isBlockedByAnimation() {
		return blockedByAnimation;
	}

	@Override
	protected void onGameStateChanged() {
		if (ignoreEvents()) {
			return;
		}
		setBlockedByAnimation(true);
		if (multiplayer) {
			Server.sendNotification(GameNotification.GAME_STATE_UPDATE, this, 0);
			this.switched = true;
			Server.sendNotification(GameNotification.GAME_STATE_UPDATE, this, 1);
			this.switched = false;
		} else NotificationProxy.sendNotification(GameNotification.GAME_STATE_UPDATE, this);


		if (multiplayer) {
			try {
				boolean connectingClient = (boolean) Server.getInFromConnectingClientStream().readObject();
				boolean hostClient = (boolean) Server.getInFromHostClientStream().readObject();
			} catch (IOException e) {
				if (exceptions) {
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				if (exceptions) {
					e.printStackTrace();
				}
			}
		} else {
			while (blockedByAnimation) {
				try {
					Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
				} catch (InterruptedException e) {
				}
			}
		}
		setBlockedByAnimation(false);
		getGameEvents().clear();
		if (multiplayer) {
			Server.sendNotification(GameNotification.GAME_STATE_LATE_UPDATE, this, 0);
			this.getPlayer1().setHideCards(true);
			this.switched = true;
			Server.sendNotification(GameNotification.GAME_STATE_LATE_UPDATE, this, 1);
			this.switched = false;
		} else NotificationProxy.sendNotification(GameNotification.GAME_STATE_LATE_UPDATE, this);
	}

	public void setBlockedByAnimation(boolean blockedByAnimation) {
		this.blockedByAnimation = blockedByAnimation;
	}

}
