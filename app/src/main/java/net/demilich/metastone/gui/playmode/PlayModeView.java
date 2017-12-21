package net.demilich.metastone.gui.playmode;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Server;
import net.demilich.metastone.game.behaviour.human.HumanActionOptions;
import net.demilich.metastone.game.behaviour.human.HumanTargetOptions;
import net.demilich.metastone.game.behaviour.human.MultiplayerBehaviour;
import net.demilich.metastone.gui.multiplayermode.Client;

public class PlayModeView extends BorderPane {

	@FXML
	private Button backButton;

	@FXML
	private VBox sidePane;

	@FXML
	private Pane navigationPane;

	private final GameBoardView boardView;
	private final HumanActionPromptView actionPromptView;

	private final LoadingBoardView loadingView;

	private boolean firstUpdate = true;

	public PlayModeView(boolean multiplayer) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/PlayModeView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		boardView = new GameBoardView();
		// setCenter(boardView);
		loadingView = new LoadingBoardView();
		setCenter(loadingView);

		actionPromptView = new HumanActionPromptView(multiplayer);
		//sidePane.getChildren().add(actionPromptView);

		backButton.setOnAction(actionEvent -> {
			try {
				Server.rip();
				Client.rip();
			} catch (IOException e) {
				NotificationProxy.sendNotification(GameNotification.MAIN_MENU);
				e.printStackTrace();
			}

		});

		sidePane.getChildren().setAll(actionPromptView, navigationPane);
	}

	public void disableTargetSelection() {
		boardView.disableTargetSelection();
		actionPromptView.setVisible(true);
	}

	public void enableTargetSelection(HumanTargetOptions targetOptions) {
		boardView.enableTargetSelection(targetOptions);
	}

	public HumanActionPromptView getActionPromptView() {
		return actionPromptView;
	}

	public void showAnimations(GameContext context) {
		boardView.showAnimations(context);
	}

	public void updateGameState(GameContext context) {
		if (firstUpdate) {
			setCenter(boardView);
			firstUpdate = false;
		}
		if (context.getPlayer1().getBehaviour() instanceof MultiplayerBehaviour && context.switched) {
			context.getPlayer2().setHideCards(false);
			context.getPlayer1().setHideCards(true);
		}
		if (context.getPlayer2().getBehaviour() instanceof MultiplayerBehaviour && !context.switched) {
			context.getPlayer1().setHideCards(false);
			context.getPlayer2().setHideCards(true);
		}
		boardView.updateGameState(context);
		if (context.gameDecided()) {
			sidePane.getChildren().clear();
			sidePane.getChildren().add(backButton);
		}
	}
	
	public void doCardActionStuff(HumanActionOptions options) {
		boardView.doCardActionStuff(options);
	}

}
