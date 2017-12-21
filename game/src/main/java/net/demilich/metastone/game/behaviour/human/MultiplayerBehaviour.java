package net.demilich.metastone.game.behaviour.human;

import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.Server;
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

	@Override @SuppressWarnings("unchecked")
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		if (context.ignoreEvents()) {
			return new ArrayList<Card>();
		}
		HumanMulliganOptions options = new HumanMulliganOptions(player, this, cards);

		if (player.equals(context.getPlayer2())) {
			Server.sendNotification(GameNotification.HUMAN_PROMPT_FOR_MULLIGAN, options, 1);
			mulliganCards = (List<Card>) Server.readObject(1);
		} else {
			Server.sendNotification(GameNotification.HUMAN_PROMPT_FOR_MULLIGAN, options, 0);
			mulliganCards = (List<Card>) Server.readObject(0);
		}

		return mulliganCards;
	}

	@Override
	public void onActionSelected(GameAction action) {
		this.selectedAction = action;
		waitingForInput = false;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		HumanActionOptions options = new HumanActionOptions(this, context, player, validActions, true);
		if (player.equals(context.getPlayer2())) {
			HumanActionOptions options1 = options;
			options1.getContext().switchy();
			//System.out.println(options1.getPlayer().getHand().toList().toString());
			Server.sendNotification(GameNotification.HUMAN_PROMPT_FOR_ACTION, options1, 1);
			selectedAction = (GameAction) Server.readObject(1);
		} else {
			//System.out.println(options.getPlayer().getHand().toList().toString());
			Server.sendNotification(GameNotification.HUMAN_PROMPT_FOR_ACTION, options, 0);
			selectedAction = (GameAction) Server.readObject(0);
		}

		return selectedAction;
	}

	@Override
	public void setMulliganCards(List<Card> mulliganCards) {
		this.mulliganCards = mulliganCards;
		this.waitingForInput = false;
	}

}
