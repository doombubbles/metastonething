package net.demilich.metastone.gui.cards;

import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.gui.IconFactory;

public class HandCard extends CardToken {

	@FXML
	private Pane topPane;
	@FXML
	private Pane centerPane;
	@FXML
	private Pane bottomPane;

	private CardTooltip tooltipContent;
	private Tooltip tooltip;

	public HandCard() {
		super("HandCard.fxml");
		hideCard(true);
	}

	private void hideCard(boolean hide) {
		topPane.setVisible(!hide);
		centerPane.setVisible(!hide);
		bottomPane.setVisible(!hide);
		if (hide) {
			BackgroundSize size = new BackgroundSize(getWidth(), getHeight(), false, false, true, false);
			BackgroundImage image = new BackgroundImage(IconFactory.getDefaultCardBack(), BackgroundRepeat.NO_REPEAT,
					BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, size);
			Background background = new Background(image);
			setBackground(background);
		}
	}

	@Override
	public void setCard(GameContext context, Card card, Player player) {
		super.setCard(context, card, player);
		if (tooltipContent == null) {
			tooltip = new Tooltip();
			tooltipContent = new CardTooltip();
			tooltipContent.setCard(context, card, player);
			tooltip.setGraphic(tooltipContent);
			Tooltip.install(this, tooltip);
		} else {
			tooltipContent.setCard(context, card, player);
		}
		
		if (card.getCardId() == CardCatalogue.getCardById("spell_glacial_spike").getCardId()) {
			Map<String, Map<Integer, Integer>> cardIds = player.getStatistics().getCardsPlayed();
			int count = 0;
			for (String cardId : cardIds.keySet()) {
				if ((Race) context.getCardById(cardId).getAttribute(Attribute.RACE) == Race.FROST) {
					for (Integer turn : cardIds.get(cardId).keySet()) {
						count += cardIds.get(cardId).get(turn);
					}
				}
			}
			tooltipContent.descriptionLabel.setText(card.getDescription() + " [" + count + "]");
		}
		if (card.getCardId() == CardCatalogue.getCardById("spell_crusade").getCardId()) {
			Map<String, Map<Integer, Integer>> minionIds = player.getStatistics().getMinionsSummoned();
			int count = 0;
			for (String minionId : minionIds.keySet()) {
				if (minionId == context.getCardById("token_silver_hand_recruit").getCardId())
					for (Integer turn : minionIds.get(minionId).keySet()) {
						count += minionIds.get(minionId).get(turn);
					}

			}
			tooltipContent.descriptionLabel.setText(card.getDescription() + " [" + count + "]");
		}
		
		
		
		hideCard(player.hideCards());

		if (player.hideCards()) {
			Tooltip.uninstall(this, tooltip);
			tooltipContent = null;
			tooltip = null;
		}
	}

}
