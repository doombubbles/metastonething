package net.demilich.metastone.gui.cards;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.human.ActionGroup;
import net.demilich.metastone.game.behaviour.human.HumanActionOptions;
import net.demilich.metastone.game.behaviour.human.HumanTargetOptions;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
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

	private HumanActionOptions options;
	
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

	@SuppressWarnings("unchecked")
	@Override
	public void setCard(GameContext context, Card card, Player player) {
		super.setCard(context, card, player);
		super.evaluateGlow(context, card, player);
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
		
		super.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				play(event);
			}
			
		});
		super.setOnMouseMoved(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				gloww();
			}
		});
		
		hideCard(player.hideCards());

		if (player.hideCards()) {
			Tooltip.uninstall(this, tooltip);
			tooltipContent = null;
			tooltip = null;
		}
	}
	
	public void setOptions(HumanActionOptions actionOptions) {
		options = actionOptions;
	}
	
	public void play(MouseEvent mouseEvent) {
		if (options != null) {
			GameContext context = options.getContext();
			
			super.evaluateGlow(context, card, context.getActivePlayer());
			
			HandCard handCard = (HandCard) mouseEvent.getSource();
			Card card = handCard.getCard();
			if (card.getOwner() != context.getActivePlayerId()) {
				return;
			}
			if (options.getValidActions().size() < 2 || !options.getBehaviour().isWaiting()) {
				return;
			}
			Collection<ActionGroup> actionGroups = new ArrayList<>();
			for (GameAction action : options.getValidActions()) {
				if (!options.matchesExistingGroup(action, actionGroups)) {
					ActionGroup newActionGroup = new ActionGroup(action);
					actionGroups.add(newActionGroup);
				}
			}
			ActionGroup yeah = null;
			for (ActionGroup actionGroup : actionGroups) {
				GameAction action = actionGroup.getPrototype();
				if (context.resolveSingleTarget(action.getSource()) == card) {
					yeah = actionGroup;
				}
			}
			if (yeah.getActionsInGroup().size() == 1 && (yeah.getPrototype().getTargetRequirement() == TargetSelection.NONE || yeah.getPrototype().getActionType() == ActionType.SUMMON)) {
				options.getBehaviour().onActionSelected(yeah.getPrototype());
				NotificationProxy.sendNotification(GameNotification.HIDE_ACTIONS, options);
			} else {
				HumanTargetOptions humanTargetOptions = new HumanTargetOptions(options.getBehaviour(), context, options.getPlayer().getId(), yeah);
				NotificationProxy.sendNotification(GameNotification.HIDE_ACTIONS, options);
				NotificationProxy.sendNotification(GameNotification.HUMAN_PROMPT_FOR_TARGET, humanTargetOptions);
			}
			
		}
	}
	
	public void gloww() {
		if (options != null) {
			GameContext context = options.getContext();
			if (context.getTurn() > 1 || !context.getActivePlayer().getStatistics().getCardsPlayed().isEmpty()) {
				return;
			}
			super.evaluateGlow(context, card, context.getActivePlayer());
		}
	}
}
