package net.demilich.metastone.gui.multiplayermode;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import net.demilich.metastone.game.behaviour.GreedyOptimizeMove;
import net.demilich.metastone.game.behaviour.IBehaviour;
import net.demilich.metastone.game.behaviour.NoAggressionBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.WeightedHeuristic;
import net.demilich.metastone.game.behaviour.human.HumanBehaviour;
import net.demilich.metastone.game.behaviour.threat.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFactory;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.MetaDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.heroes.MetaHero;
import net.demilich.metastone.game.gameconfig.MultiplayerConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.gui.IconFactory;
import net.demilich.metastone.gui.common.DeckStringConverter;
import net.demilich.metastone.gui.common.HeroStringConverter;
import net.demilich.metastone.gui.playmode.config.PlayerConfigType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiplayerConfigView extends VBox {

	@FXML
	protected Label heroNameLabel;

	@FXML
	protected ImageView heroIcon;

	@FXML
	protected ComboBox<HeroCard> heroBox;

	@FXML
	protected ComboBox<Deck> deckBox;

	@FXML
	protected TextField ipAddress;

	@FXML
	protected TextField port;

	private final PlayerConfig playerConfig = new PlayerConfig();

	private List<Deck> decks = new ArrayList<Deck>();

	private PlayerConfigType selectionHint;

	private DeckFormat deckFormat;

	public MultiplayerConfigView(PlayerConfigType selectionHint) {
		this.selectionHint = selectionHint;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MultiplayerConfigView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		heroBox.setConverter(new HeroStringConverter());
		deckBox.setConverter(new DeckStringConverter());

		setupHeroes();
		deckBox.valueProperty().addListener((ChangeListener<Deck>) (observableProperty, oldDeck, newDeck) -> {
			getPlayerConfig().setDeck(newDeck);
		});
	}

	private void filterDecks() {
		HeroClass heroClass = getPlayerConfig().getHeroCard().getHeroClass();
		ObservableList<Deck> deckList = FXCollections.observableArrayList();

		if (heroClass == HeroClass.DECK_COLLECTION) {
			for (Deck deck : decks) {
				if (deck.getHeroClass() != HeroClass.DECK_COLLECTION) {
					continue;
				}
				MetaDeck metaDeck = (MetaDeck) deck;
				List<Deck> metaDecks = new ArrayList<Deck>();
				metaDeck.getDecks().forEach(p -> {
					metaDecks.add(p);
				});
				MetaDeck metaDick = metaDeck;
				for (Deck dick : metaDecks) {
					if (deckFormat == null || !deckFormat.isInFormat(dick)) {
						metaDick.getDecks().remove(dick);
					}
				}
				if (deckFormat != null && deckFormat.isInFormat(deck)) {
					deckList.add((Deck) metaDick);
				}
				
			}
		} else {
			Deck randomDeck = DeckFactory.getRandomDeck(heroClass, deckFormat);
			deckList.add(randomDeck);
			for (Deck deck : decks) {
				if (deck.getHeroClass() == HeroClass.DECK_COLLECTION) {
					continue;
				}
				if (deck.getHeroClass() == heroClass || deck.getHeroClass() == HeroClass.ANY) {
					if (deckFormat != null && deckFormat.isInFormat(deck)) {
						deckList.add(deck);
					}
				}
			}
		}

		deckBox.setItems(deckList);
		deckBox.getSelectionModel().selectFirst();
	}

	public PlayerConfig getPlayerConfig() {
		return playerConfig;
	}

	public void injectDecks(List<Deck> decks) {
		this.decks = decks;
		heroBox.getSelectionModel().selectFirst();
	}

	private void selectHero(HeroCard heroCard) {
		Image heroPortrait;
		if (heroCard.getHeroClass() == HeroClass.DECK_COLLECTION) {
			heroPortrait = new Image(IconFactory.getHeroIconUrl(null));
		} else heroPortrait = new Image(IconFactory.getHeroIconUrl(heroCard));
		heroIcon.setImage(heroPortrait);
		heroNameLabel.setText(heroCard.getName());
		getPlayerConfig().setHeroCard(heroCard);
		filterDecks();
	}

	public void setupBehaviours() {
		ObservableList<IBehaviour> behaviourList = FXCollections.observableArrayList();
		if (selectionHint == PlayerConfigType.HUMAN || selectionHint == PlayerConfigType.SANDBOX) {
			behaviourList.add(new HumanBehaviour());
		}

		behaviourList.add(new GameStateValueBehaviour());

		if (selectionHint == PlayerConfigType.OPPONENT) {
			behaviourList.add(new HumanBehaviour());
		}

		behaviourList.add(new PlayRandomBehaviour());

		behaviourList.add(new GreedyOptimizeMove(new WeightedHeuristic()));
		behaviourList.add(new NoAggressionBehaviour());

	}

	public void setupHeroes() {
		ObservableList<HeroCard> heroList = FXCollections.observableArrayList();
		for (Card card : CardCatalogue.getHeroes()) {
			heroList.add((HeroCard) card);
		}

		heroList.add(new MetaHero());

		heroBox.setItems(heroList);
		heroBox.valueProperty().addListener((ChangeListener<HeroCard>) (observableValue, oldHero, newHero) -> {
			selectHero(newHero);
		});
	}

	public void setDeckFormat(DeckFormat newDeckFormat) {
		deckFormat = newDeckFormat;
		filterDecks();
	}

	public String getIpAddress() {
		return ipAddress.getText();
	}

	public int getPort() {
		if (port.getText().equals("")) {
			return -1;
		}
		return Integer.parseInt(port.getText());
	}

}
