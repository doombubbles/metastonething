package net.demilich.metastone.gui.playmode.animation;

import net.demilich.metastone.gui.multiplayermode.Client;
import net.demilich.nittygrittymvc.Proxy;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.gui.playmode.GameContextVisualizable;

import java.io.IOException;

public class AnimationProxy extends Proxy<GameNotification> {

	public static final String NAME = "AnimationProxy";

	private GameContextVisualizable context;
	private int animationsRunning;

	public AnimationProxy() {
		super(NAME);
	}

	public void animationCompleted() {
		if (--animationsRunning == 0) {
			if (context.isMultiplayer()) {
				try {
					Client.getOutToServerStream().writeObject(false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else context.setBlockedByAnimation(false);
		}
	}

	public void animationStarted() {
		animationsRunning++;
	}

	public GameContextVisualizable getContext() {
		return context;
	}

	public void setContext(GameContextVisualizable context) {
		this.context = context;
	}

}
