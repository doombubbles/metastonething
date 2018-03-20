package net.demilich.metastone.gui.playmode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javafx.scene.image.ImageView;
import net.demilich.metastone.game.entities.minions.Rift;
import net.demilich.metastone.gui.multiplayermode.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.human.ActionGroup;
import net.demilich.metastone.game.behaviour.human.HumanActionOptions;
import net.demilich.metastone.game.behaviour.human.HumanTargetOptions;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.PermanentCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Permanent;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.gui.cards.CardTooltip;
import net.demilich.metastone.gui.cards.HandCard;

public class SummonToken extends GameToken {
	@FXML
	private Label name;
	@FXML
	private Group attackAnchor;
	@FXML
	private Group hpAnchor;

	@FXML
	private ImageView attack_icon;

	@FXML
	private ImageView health_icon;

	@FXML
	private Node defaultToken;
	@FXML
	private Node divineShield;
	@FXML
	private Node taunt;
	@FXML
	private Text windfury;
	@FXML
	private Node deathrattle;
	
	@FXML
	private Node poisonous;
	
	@FXML
	private Node lifesteal;
	
	@FXML
	private Node trigger;
	
	@FXML
	private Node spell_damage;

	@FXML
	private Shape frozen;
	
	@FXML
	private Shape elusive;
	
	@FXML
	private Shape canAttack;
	
	@FXML
	private Node immune;
	
	@FXML
	private Shape elusivetaunt;
	
	@FXML
	private Shape canAttackTaunt;

	private CardTooltip cardTooltip;
	
	private HumanActionOptions options;
	private boolean multiplayer;
	private boolean switched;
	
	private Summon summon;
	
	Logger logger = LoggerFactory.getLogger(SummonToken.class);

	public SummonToken() {
		super("SummonToken.fxml");
		Tooltip tooltip = new Tooltip();
		cardTooltip = new CardTooltip();
		tooltip.setGraphic(cardTooltip);
		Tooltip.install(this, tooltip);
		frozen.getStrokeDashArray().add(16.0);
		elusive.getStrokeDashArray().add(16.0);
		elusivetaunt.getStrokeDashArray().add(16.0);
	}

	public void setSummon(Summon summon) {
		name.setText(summon.getName());
		if (summon instanceof Minion) {
			attackAnchor.setVisible(true);
			attack_icon.setVisible(true);
			hpAnchor.setVisible(true);
			health_icon.setVisible(true);
			setScoreValue(attackAnchor, summon.getAttack(), summon.getAttributeValue(Attribute.BASE_ATTACK));
			setScoreValue(hpAnchor, summon.getHp(), summon.getBaseHp(), summon.getMaxHp());
		} else if (summon instanceof Permanent) {
			if (summon instanceof Rift) {
				hpAnchor.setVisible(true);
				setScoreValue(hpAnchor, ((Rift) summon).getTime(), ((Rift) summon).getTotalDuration());
			} else hpAnchor.setVisible(false);
			attackAnchor.setVisible(false);
			attack_icon.setVisible(false);
			health_icon.setVisible(false);
		}
		this.summon = summon;
		visualizeStatus(summon);
		if (options != null) {
			cardTooltip.setCard(options.getContext(), summon.getSourceCard(), options.getContext().getPlayer(summon.getOwner()));
		} else cardTooltip.setCard(summon.getSourceCard());
		this.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				attack(event);
			}
		});
	}
	
	

	private void visualizeStatus(Summon summon) {
		taunt.setVisible(summon.hasAttribute(Attribute.TAUNT));
		defaultToken.setVisible(!summon.hasAttribute(Attribute.TAUNT));
		divineShield.setVisible(summon.hasAttribute(Attribute.DIVINE_SHIELD));
		immune.setVisible(summon.hasAttribute(Attribute.IMMUNE));
		windfury.setVisible(summon.hasAttribute(Attribute.WINDFURY) || summon.hasAttribute(Attribute.MEGA_WINDFURY));
		if(summon.hasAttribute(Attribute.MEGA_WINDFURY)) {
			windfury.setText("x4");
		} else {
			windfury.setText("x2");
		}
		deathrattle.setVisible(summon.hasAttribute(Attribute.DEATHRATTLES));
		poisonous.setVisible(summon.hasAttribute(Attribute.POISONOUS));
		lifesteal.setVisible(summon.hasAttribute(Attribute.LIFESTEAL));
		frozen.setVisible(summon.hasAttribute(Attribute.FROZEN));
		trigger.setVisible(summon.hasProbablyVisibleTrigger());
		
		elusive.setVisible((summon.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS) || summon.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)) && !summon.hasAttribute(Attribute.TAUNT));
		elusivetaunt.setVisible((summon.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS) || summon.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS))  && summon.hasAttribute(Attribute.TAUNT));
		visualizeStealth(summon);
		spell_damage.setVisible(summon.hasAttribute(Attribute.SPELL_DAMAGE));
		canAttack.setVisible(false);
		canAttackTaunt.setVisible(false);
		if (options != null) {
			if (((multiplayer && switched) ? options.getContext().getPlayer2() : options.getContext().getPlayer1()).getSummons().contains(summon)
					&& ((multiplayer && switched) ? options.getContext().getPlayer2() : options.getContext().getPlayer1()) == options.getContext().getActivePlayer()) {
				canAttack.setVisible(summon.getEntityType().equals(EntityType.MINION) && summon.canAttackThisTurn() && !summon.hasAttribute(Attribute.TAUNT));
				canAttackTaunt.setVisible(summon.getEntityType().equals(EntityType.MINION) && summon.canAttackThisTurn() && summon.hasAttribute(Attribute.TAUNT));
			}
			
		}
		
	}

	private void visualizeStealth(Summon summon) {
		Node token = summon.hasAttribute(Attribute.TAUNT) ? taunt : defaultToken;
		token.setOpacity(summon.hasAttribute(Attribute.STEALTH) ? 0.5 : 1);
	}
	
	public void setOptions(HumanActionOptions actionOptions, boolean switched) {
		options = actionOptions;
		multiplayer = options.isMultiplayer();
		this.switched  = switched;
	}
	
	public void attack(MouseEvent mouseEvent) {
		if (options != null) {
			GameContext context = options.getContext();
			SummonToken summonToken = (SummonToken) mouseEvent.getSource();
			if (this.summon.getOwner() != context.getActivePlayerId() || multiplayer ? Client.blockedByAnimation : !options.getBehaviour().isWaiting()) {
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
				if (context.resolveSingleTarget(action.getSource()) == null) {
					break;
				}
				if (context.resolveSingleTarget(action.getSource()).getId() == this.summon.getId()) {
					yeah = actionGroup;
				}
			}
			if (yeah == null) {
				return;
			}
			if (yeah.getActionsInGroup().size() == 1 && (yeah.getPrototype().getTargetRequirement() == TargetSelection.NONE || yeah.getPrototype().getActionType() == ActionType.SUMMON)) {
				if (multiplayer) {
					try {
						Client.getOutToServerStream().writeObject(yeah.getPrototype());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else options.getBehaviour().onActionSelected(yeah.getPrototype());
				NotificationProxy.sendNotification(GameNotification.HIDE_ACTIONS, options);
			} else {
				HumanTargetOptions humanTargetOptions = new HumanTargetOptions(options.getBehaviour(), context, options.getPlayer().getId(), yeah, multiplayer);
				NotificationProxy.sendNotification(GameNotification.HUMAN_PROMPT_FOR_TARGET, humanTargetOptions);
				NotificationProxy.sendNotification(GameNotification.HIDE_ACTIONS, options);
			}
		}
	}

}
