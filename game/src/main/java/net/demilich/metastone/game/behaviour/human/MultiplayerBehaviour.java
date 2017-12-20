package net.demilich.metastone.game.behaviour.human;

import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.IActionSelectionListener;
import net.demilich.metastone.game.cards.Card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MultiplayerBehaviour extends HumanBehaviour implements IActionSelectionListener, Serializable {

	private GameAction selectedAction;
	private boolean waitingForInput = false;
	private List<Card> mulliganCards;

	@Override
	public String getName() {
		return "<Multiplayer controlled>";
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		if (context.ignoreEvents()) {
			return new ArrayList<Card>();
		}
		waitingForInput = true;
		HumanMulliganOptions options = new HumanMulliganOptions(player, this, cards, context.getOpponent(player));


		NotificationProxy.sendNotification(GameNotification.SERVER_PROMPT_FOR_MULLIGAN, options);

		while (waitingForInput) {
			try {
				System.out.println("waiting for mulligan info to come back... " + waitingForInput);
				Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
			} catch (InterruptedException e) {
			}
		}

		System.out.println("receivey2");
		return mulliganCards;
	}

	@Override
	public void onActionSelected(GameAction action) {
		this.selectedAction = action;
		waitingForInput = false;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		waitingForInput = true;
		HumanActionOptions options = new HumanActionOptions(this, context, player, validActions, context.getOpponent(player));

		NotificationProxy.sendNotification(GameNotification.SERVER_PROMPT_FOR_ACTION, options);

		while (waitingForInput) {
			try {
				Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
				if (context.ignoreEvents()) {
					return null;
				}
			} catch (InterruptedException e) {
			}
		}
		return selectedAction;
	}

	@Override
	public void setMulliganCards(List<Card> mulliganCards) {
		System.out.println("waitingForInput is currently: " + waitingForInput);
		this.mulliganCards = mulliganCards;
		this.waitingForInput = false;
		System.out.println("waitingForInput is currently: " + waitingForInput);
	}

}
