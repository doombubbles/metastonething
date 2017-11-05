package net.demilich.metastone.gui.cards;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
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
		super.nameLabel.getStyleClass().remove("thickBorder");
		super.nameLabel.getStyleClass().add("thinBorder");
		evaluateGlow(context, card, player);
		if (tooltipContent == null) {
			tooltip = new Tooltip();
			tooltipContent = new CardTooltip();
			tooltipContent.setCard(context, card, player);
			tooltip.setGraphic(tooltipContent);
			Tooltip.install(this, tooltip);
		} else {
			tooltipContent.setCard(context, card, player);
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
			List<ActionGroup> yeahs = new ArrayList<ActionGroup>();
			ActionGroup yeah = null;
			for (ActionGroup actionGroup : actionGroups) {
				GameAction action = actionGroup.getPrototype();
				if (context.resolveSingleTarget(action.getSource()) == card) {
					yeahs.add(actionGroup);
				}
			}
			if (yeahs.size() > 1 && card.hasAttribute(Attribute.CHOOSE_ONE)) {
				switch (mouseEvent.getButton()) {
				case PRIMARY:
						yeahs.remove(1);
					break;
				case SECONDARY:
						yeahs.remove(0);
					break;
				}
				
			}
			if (yeahs.isEmpty()) {
				return;
			}
			yeah = yeahs.get(0);
			
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
			evaluateGlow(context, card, context.getActivePlayer());
		}
	}
	
	public void glow(String s) {
		switch (s) {
			case "YELLOW":
				super.setBorder(new Border(new BorderStroke(Color.GOLD, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(2), new Insets(-1.0))));
				break;
			case "GREEN":
				super.setBorder(new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(2), new Insets(-1.0))));
				break;
			case "NOPE":
				super.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(1))));
				break;
		}
		
	}
	public void evaluateGlow(GameContext context, Card card, Player player) {
		glow("NOPE");
		if (context.getLogic().canPlayCard(player.getId(), card.getCardReference()) && context.getActivePlayer() == player) {
			glow("GREEN");
			if (card.hasAttribute(Attribute.COMBO) && player.hasAttribute(Attribute.COMBO)) {
				glow("YELLOW");
				return;
			}
			if (card.getGlow() != null) {
				int i = 0;
				Player targetPlayer = player;
				if (card.getGlow().get(ConditionArg.TARGET_PLAYER) != null) {
					switch ((TargetPlayer) card.getGlow().get(ConditionArg.TARGET_PLAYER)) {
					case ACTIVE:
						targetPlayer = context.getActivePlayer();
						break;
					case BOTH:
						targetPlayer = context.getOpponent(player);
						if (card.getGlow().get(ConditionArg.TARGET) != null) {
							for (Entity entity : context.resolveTarget(player, card, (EntityReference) card.getGlow().get(ConditionArg.TARGET))) {
								i += card.getGlow().create().isFulfilled(context, player, card, entity) ? 1 : 0;
							} 
						} else i += card.getGlow().create().isFulfilled(context, player, card, null) ? 1 : 0;
						break;
					case INACTIVE:
						targetPlayer = context.getOpponent(context.getActivePlayer());
						break;
					case OPPONENT:
						targetPlayer = context.getOpponent(player);
						break;
					case SELF:
						targetPlayer = player;
						break;
					default:
						break;
					}
				}
				if (card.getGlow().get(ConditionArg.TARGET) != null) {
					for (Entity entity : context.resolveTarget(player, card, (EntityReference) card.getGlow().get(ConditionArg.TARGET))) {
						i += card.getGlow().create().isFulfilled(context, targetPlayer, card, entity) ? 1 : 0;
					}
				} else i += card.getGlow().create().isFulfilled(context, targetPlayer, card, null) ? 1 : 0;
				
				
				if (i > 0) {
					glow("YELLOW");
				}
				
				
			}
		}
	}
}
