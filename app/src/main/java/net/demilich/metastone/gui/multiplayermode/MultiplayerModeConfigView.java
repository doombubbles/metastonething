package net.demilich.metastone.gui.multiplayermode;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.human.MultiplayerBehaviour;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.gameconfig.GameConfig;
import net.demilich.metastone.game.gameconfig.MultiplayerConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.gui.common.DeckFormatStringConverter;
import net.demilich.metastone.gui.gameconfig.PlayerConfigView;
import net.demilich.metastone.gui.playmode.config.PlayerConfigType;

import java.io.IOException;
import java.util.List;

public class MultiplayerModeConfigView extends BorderPane implements EventHandler<ActionEvent> {

	@FXML
	protected ComboBox<DeckFormat> formatBox;

	@FXML
	protected HBox playerArea;

	@FXML
	protected Button startButton;

	@FXML
	protected Button backButton;

	protected MultiplayerConfigView player1Config;

	private List<DeckFormat> deckFormats;

	public MultiplayerModeConfigView() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/PlayModeConfigView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		formatBox.setConverter(new DeckFormatStringConverter());

		player1Config = new MultiplayerConfigView(PlayerConfigType.HUMAN);

		playerArea.getChildren().add(player1Config);

		startButton.setOnAction(this);
		backButton.setOnAction(this);

		formatBox.valueProperty().addListener((ChangeListener<DeckFormat>) (observableProperty, oldDeckFormat, newDeckFormat) -> {
			setDeckFormats(newDeckFormat);
		});
	}

	private void setupDeckFormats() {
		ObservableList<DeckFormat> deckFormatList = FXCollections.observableArrayList();

		for (DeckFormat deckFormat : deckFormats) {
			deckFormatList.add(deckFormat);
		}

		formatBox.setItems(deckFormatList);
		formatBox.getSelectionModel().selectFirst();
	}

	private void setDeckFormats(DeckFormat newDeckFormat) {
		player1Config.setDeckFormat(newDeckFormat);
	}

	@Override
	public void handle(ActionEvent actionEvent) {
		if (actionEvent.getSource() == startButton) {
			MultiplayerConfig multiPlayerConfig = new MultiplayerConfig();
			multiPlayerConfig.setNumberOfGames(1);
			PlayerConfig playerConfig = player1Config.getPlayerConfig();
			playerConfig.setBehaviour(new MultiplayerBehaviour());
			multiPlayerConfig.setPlayerConfig1(playerConfig);
			multiPlayerConfig.setDeckFormat(formatBox.getValue());
			multiPlayerConfig.setIpAddress(player1Config.getIpAddress());
			multiPlayerConfig.setPort(player1Config.getPort());
			if (player1Config.getPort() == -1) {
				return;
			}
			NotificationProxy.sendNotification(GameNotification.COMMIT_MULTIPLAYER_CONFIG, multiPlayerConfig);



		} else if (actionEvent.getSource() == backButton) {
			NotificationProxy.sendNotification(GameNotification.MAIN_MENU);
		}
	}

	public void injectDecks(List<Deck> decks) {
		player1Config.injectDecks(decks);
	}

	public void injectDeckFormats(List<DeckFormat> deckFormats) {
		this.deckFormats = deckFormats;
		setupDeckFormats();
		player1Config.setDeckFormat(formatBox.getValue());
	}

}
