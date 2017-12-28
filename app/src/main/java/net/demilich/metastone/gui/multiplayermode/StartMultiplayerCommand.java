package net.demilich.metastone.gui.multiplayermode;

import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.gameconfig.GameConfig;
import net.demilich.metastone.game.gameconfig.MultiplayerConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.gui.playmode.GameContextVisualizable;
import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;

public class StartMultiplayerCommand extends SimpleCommand<GameNotification> {

	@Override
	public void execute(INotification<GameNotification> notification) {
		MultiplayerConfig multiplayerConfig = (MultiplayerConfig) notification.getBody();

		PlayerConfig playerConfig1 = multiplayerConfig.getPlayerConfig1();
		PlayerConfig playerConfig2 = multiplayerConfig.getPlayerConfig2();

		Player player1 = new Player(playerConfig1);
		Player player2 = new Player(playerConfig2);

		DeckFormat deckFormat = multiplayerConfig.getDeckFormat();
		GameContext newGame = new GameContextVisualizable(player1, player2, new GameLogic(), deckFormat, true);
		NotificationProxy.sendNotification(GameNotification.PLAY_GAME, newGame);
	}

}
