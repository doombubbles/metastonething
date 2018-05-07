package net.demilich.metastone.gui.playmode;

import java.io.IOException;
import java.util.*;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.human.ActionGroup;
import net.demilich.metastone.game.behaviour.human.HumanActionOptions;
import net.demilich.metastone.game.behaviour.human.HumanTargetOptions;
import net.demilich.metastone.game.behaviour.human.MultiplayerBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.QuestCard;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.heroes.powers.HeroPower;
import net.demilich.metastone.gui.IconFactory;
import net.demilich.metastone.gui.cards.CardTooltip;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.gui.multiplayermode.Client;

public class HeroToken extends GameToken {
	
	@FXML
	private StackPane targetAnchor;
	
	@FXML
	private Group attackAnchor;
	@FXML
	private Group hpAnchor;
	@FXML
	private Label cardsLabel;
	@FXML
	private Label manaLabel;


	@FXML
	private Text windfury;
	@FXML
	private Label questLabel;

	@FXML
	private Group armorAnchor;
	@FXML
	private ImageView armorIcon;

	@FXML
	private Pane weaponPane;
	@FXML
	private Label weaponNameLabel;
	@FXML
	private Group weaponAttackAnchor;
	@FXML
	private Group weaponDurabilityAnchor;

	@FXML
	private ImageView portrait;

	@FXML
	private Group heroPowerAnchor;
	@FXML
	private ImageView heroPowerIcon;
	@FXML
	private Shape glow;

	@FXML
	private Pane heroPowerPane2;
	@FXML
	private Group heroPowerAnchor2;
	@FXML
	private ImageView heroPowerIcon2;
	@FXML
	private Shape glow3;

	@FXML
	private ImageView elusive;
	
	@FXML
	private ImageView glow2;

	@FXML
	private Pane secretsAnchor;

	@FXML
	private Shape frozen;
	
	@FXML
	private Shape divineShield;
	
	@FXML
	private Node trigger;
	
	@FXML
	private Node deathrattle;
	
	@FXML
	private Node lifesteal;
	
	@FXML
	private Node poisonous;
	
	@FXML
	private ImageView immune;
	
	@FXML
	private ImageView shadowform;
	
	@FXML
	private ImageView stealth;
	
	private Hero hero;
	
	private HumanActionOptions options;
	private boolean multiplayer;

	public HeroToken() {
		super("HeroToken.fxml");
		frozen.getStrokeDashArray().add(16.0);
	}

	public void highlight(boolean highlight) {
		String cssBorder = null;
		if (highlight) {
			cssBorder = "-fx-border-color:seagreen; \n" + "-fx-border-radius:7;\n" + "-fx-border-width:5.0;";
		} else {
			cssBorder = "-fx-border-color:transparent; \n" + "-fx-border-radius:7;\n" + "-fx-border-width:5.0;";
		}

		target.setStyle(cssBorder);
	}

	public void setHero(Player player, GameContext context) {
		hero = player.getHero();
		setScoreValue(attackAnchor, hero.getAttack());
		Image portraitImage = new Image(IconFactory.getHeroIconUrl(hero.getSourceCard()));
		portrait.setImage(portraitImage);
		setScoreValue(hpAnchor, hero.getHp(), hero.getAttributeValue(Attribute.BASE_HP), hero.getMaxHp());
		if (!player.getDeck().isEmpty()) {
			cardsLabel.setText("Cards in deck: " + player.getDeck().getCount());
		} else {
			cardsLabel.setText("Fatigue: " + player.getAttributeValue(Attribute.FATIGUE));
		}
		
		
		if (!player.getQuests().isEmpty()) {
			int n = context.getCardById((String) player.getQuests().toArray()[player.getQuests().size() - 1]).getAttributeValue(Attribute.QUEST);
			questLabel.setText("Quest: " + (n - player.getAttributeValue(Attribute.QUEST)) + "/" + n);
		} else if (player.hasAttribute(Attribute.CTHUN_ATTACK_BUFF)) {
			questLabel.setText("C'Thun: " + (player.getAttributeValue(Attribute.CTHUN_ATTACK_BUFF) + 6));
		}
		else questLabel.setText("            ");
		glow2.setVisible(hero.getId() == ((multiplayer && context.switched) ? context.getPlayer2().getHero() : context.getPlayer1().getHero()).getId() && hero.canAttackThisTurn());
		if (player.getAttributeValue(Attribute.OVERLOAD) > 0) {
			manaLabel.setText(player.getMana() + "/" + player.getMaxMana() + "\nOver: " + player.getAttributeValue(Attribute.OVERLOAD));
		} else {
			manaLabel.setText(player.getMana() + "/" + player.getMaxMana());
		}
		updateArmor(hero.getArmor());
		updateHeroPower(hero);
		heroPowerPane2.setVisible(hero.hasHeroPower2());
		if (hero.hasHeroPower2()) {
			updateSecondHeroPower(hero);
		}
		glow.setVisible(hero.getId() == ((multiplayer && context.switched) ? context.getPlayer2().getHero() : context.getPlayer1().getHero()).getId()
						&& hero.getId() == context.getActivePlayer().getHero().getId()
						&& context.getLogic().canPlayCard(context.getPlayer(hero.getOwner()).getId(), hero.getHeroPower().getCardReference())
						&& hero.getHeroPower().getBaseManaCost() != 0);
		glow3.setVisible(hero.hasHeroPower2()
				&& hero.getId() == ((multiplayer && context.switched) ? context.getPlayer2().getHero() : context.getPlayer1().getHero()).getId()
				&& hero.getId() == context.getActivePlayer().getHero().getId()
				&& context.getLogic().canPlayCard(context.getPlayer(hero.getOwner()).getId(), hero.getHeroPower2().getCardReference())
				&& hero.getHeroPower2().getBaseManaCost() != 0);
		updateWeapon(hero.getWeapon(), !player.hasAttribute(Attribute.REPLACED_WEAPON_SLOT));
		updateSecrets(player);
		updateStatus(hero, context);
		
		heroPowerIcon.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				MouseEvent Event = (MouseEvent) event;
				heroPower(Event, 1);
			}
		});
		heroPowerIcon2.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				MouseEvent Event = (MouseEvent) event;
				heroPower(Event, 2);
			}
		});
		targetAnchor.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				attack(event);
			}
		});
	}

	private void updateArmor(int armor) {
		setScoreValue(armorAnchor, armor);
		boolean visible = armor > 0;
		armorIcon.setVisible(visible);
		armorAnchor.setVisible(visible);
	}

	private void updateHeroPower(Hero hero) {
		Image heroPowerImage;
		boolean nope = false;
		if (options != null) {
			GameContext context = options.getContext();
			Player player = context.getPlayer(hero.getOwner());
			if (!context.getLogic().hasAutoHeroPower(hero.getOwner()) && !context.getLogic().canPlayCard(hero.getOwner(), hero.getHeroPower().getCardReference(), true)) {
				heroPowerImage = new Image(IconFactory.getImageUrl("powers/nope.png"));
				nope = true;
			} else heroPowerImage = new Image(IconFactory.getHeroPowerIconUrl(hero.getHeroPower()));
		} else heroPowerImage = new Image(IconFactory.getHeroPowerIconUrl(hero.getHeroPower()));
		heroPowerAnchor.setVisible(hero.getHeroPower().getBaseManaCost() != 0 && !nope);
		heroPowerIcon.setImage(heroPowerImage);
		Card card = CardCatalogue.getCardById(hero.getHeroPower().getCardId());
		Tooltip tooltip = new Tooltip();
		CardTooltip tooltipContent = new CardTooltip();
		tooltipContent.setCard(card);
		tooltip.setGraphic(tooltipContent);
		Tooltip.install(heroPowerIcon, tooltip);
		Image portraitImage = new Image(IconFactory.getHeroIconUrl(hero.getSourceCard()));
		portrait.setImage(portraitImage);
	}

	private void updateSecondHeroPower(Hero hero) {
		Image heroPowerImage;
		boolean nope = false;
		if (options != null) {
			GameContext context = options.getContext();
			Player player = context.getPlayer(hero.getOwner());
			if (!context.getLogic().hasAutoHeroPower(hero.getOwner()) && !context.getLogic().canPlayCard(hero.getOwner(), hero.getHeroPower2().getCardReference(), true)) {
				heroPowerImage = new Image(IconFactory.getImageUrl("powers/nope.png"));
				nope = true;
			} else heroPowerImage = new Image(IconFactory.getHeroPowerIconUrl(hero.getHeroPower2()));
		} else heroPowerImage = new Image(IconFactory.getHeroPowerIconUrl(hero.getHeroPower2()));
		heroPowerAnchor2.setVisible(true);
		heroPowerIcon2.setImage(heroPowerImage);
		Card card = CardCatalogue.getCardById(hero.getHeroPower2().getCardId());
		Tooltip tooltip = new Tooltip();
		CardTooltip tooltipContent = new CardTooltip();
		tooltipContent.setCard(card);
		tooltip.setGraphic(tooltipContent);
		Tooltip.install(heroPowerIcon2, tooltip);
	}

	public void updateHeroPowerCost(GameContext context, Player player) {
		setScoreValueLowerIsBetter(heroPowerAnchor, context.getLogic().getModifiedManaCost(player, player.getHero().getHeroPower()), player.getHero().getHeroPower().getBaseManaCost());
		if (player.getHero().hasHeroPower2()) {
			setScoreValueLowerIsBetter(heroPowerAnchor2, context.getLogic().getModifiedManaCost(player, player.getHero().getHeroPower2()), player.getHero().getHeroPower2().getBaseManaCost());
		}
	}

	private void updateSecrets(Player player) {
		secretsAnchor.getChildren().clear();
		HashSet<String> secretsCopy = new HashSet<String>(player.getSecrets());
		for (String secretId : secretsCopy) {
			Card card = CardCatalogue.getCardById(secretId);
			ImageView secretIcon = null;
			if (card instanceof QuestCard) {
				secretIcon = new ImageView(IconFactory.getImageUrl("common/quest.png"));
			} else {
				if (card.getHeroClass() == HeroClass.PALADIN) {
					secretIcon = new ImageView(IconFactory.getImageUrl("common/secretyellow.png"));
				} else if (card.getHeroClass() == HeroClass.MAGE) {
					secretIcon = new ImageView(IconFactory.getImageUrl("common/secretpink.png"));
				} else if (card.getHeroClass() == HeroClass.HUNTER) {
					secretIcon = new ImageView(IconFactory.getImageUrl("common/secretgreen.png"));
				} else if (card.getHeroClass() == HeroClass.ROGUE) {
					secretIcon = new ImageView(IconFactory.getImageUrl("common/secretgrey.png"));
				} else secretIcon = new ImageView(IconFactory.getImageUrl("common/secret.png"));
			}
			secretsAnchor.getChildren().add(secretIcon);

			if (!player.isHideCards() || card instanceof QuestCard) {
				Tooltip tooltip = new Tooltip();
				CardTooltip tooltipContent = new CardTooltip();
				tooltipContent.setCard(card);
				tooltip.setGraphic(tooltipContent);
				Tooltip.install(secretIcon, tooltip);
			}
		}
	}

	private void updateStatus(Hero hero, GameContext context) {
		frozen.setVisible(hero.hasAttribute(Attribute.FROZEN));
		shadowform.setVisible(hero.hasAttribute(Attribute.SHADOWFORM));
		stealth.setVisible(hero.hasAttribute(Attribute.STEALTH));
		if (hero.getOwner() != -1) {
			immune.setVisible(hero.hasAttribute(Attribute.IMMUNE) || hasAttribute(context.getPlayer(hero.getOwner()), Attribute.IMMUNE_HERO, context));
		} else immune.setVisible(false);
		divineShield.setVisible(hero.hasAttribute(Attribute.DIVINE_SHIELD));
		elusive.setVisible(context.getLogic().hasAttribute(context.getPlayer(hero.getOwner()), Attribute.ELUSIVE_HERO));
		windfury.setVisible(hero.hasAttribute(Attribute.WINDFURY));
		windfury.setText("x2");
	}
	
	public boolean hasAttribute(Player player, Attribute attr, GameContext context) {
		return context.getLogic().hasAttribute(player, attr);
	}

	private void updateWeapon(Weapon weapon, boolean slot) {
		boolean hasWeapon = weapon != null && slot;
		weaponPane.setVisible(hasWeapon);
		if (hasWeapon) {
			weaponNameLabel.setText(weapon.getName());
			setScoreValue(weaponAttackAnchor, weapon.getWeaponDamage(), weapon.getBaseAttack());
			setScoreValue(weaponDurabilityAnchor, weapon.getDurability(), weapon.getBaseDurability(), weapon.getMaxDurability());
			Tooltip tooltip = new Tooltip();
			CardTooltip tooltipContent = new CardTooltip();
			tooltipContent.setCard(weapon.getSourceCard());
			tooltip.setGraphic(tooltipContent);
			Tooltip.install(weaponPane, tooltip);
			deathrattle.setVisible(weapon.hasAttribute(Attribute.DEATHRATTLES));
			trigger.setVisible(weapon.hasProbablyVisibleTrigger());
			poisonous.setVisible(weapon.hasAttribute(Attribute.POISONOUS));
			lifesteal.setVisible(weapon.hasAttribute(Attribute.LIFESTEAL));
		}
	}
	
	public void setOptions(HumanActionOptions targetOptions) {
		this.options = targetOptions;
		this.multiplayer = options.isMultiplayer();
	}
	
	private void heroPower(MouseEvent event, int power) {
		if (options != null) {
			GameContext context = options.getContext();
			if (hero != context.getActivePlayer().getHero() || multiplayer ? Client.blockedByAnimation : !options.getBehaviour().isWaiting()) {
				return;
			}
			HeroPower heroPower = power == 2 ? hero.getHeroPower2() : hero.getHeroPower();
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
				if (context.resolveSingleTarget(action.getSource()) == null) {
					break;
				}
				if (context.resolveSingleTarget(action.getSource()).getId() == heroPower.getId()) {
					yeahs.add(actionGroup);
				}
			}
			
			if (yeahs.size() > 1 && heroPower.hasAttribute(Attribute.CHOOSE_ONE)) {
				switch (event.getButton()) {
				case PRIMARY:
						yeahs.remove(1);
					break;
				case SECONDARY:
						yeahs.remove(0);
					break;
				default:
					break;
				}
				
			}
			if (yeahs.isEmpty()) {
				return;
			}
			yeah = yeahs.get(0);
			
			
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
	
	private void attack(Event mouseEvent) {
		if (options != null) {
			GameContext context = options.getContext();
			if (hero != context.getActivePlayer().getHero() || multiplayer ? Client.blockedByAnimation : !options.getBehaviour().isWaiting()) {
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
				if (context.resolveSingleTarget(action.getSource()).getId() == hero.getId()) {
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
	
	private void endTurn(KeyEvent keyEvent) {
		if (options != null) {
			GameContext context = options.getContext();
			if (hero != context.getActivePlayer().getHero()) {
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
				if (action.getActionType() == ActionType.END_TURN) {
					yeah = actionGroup;
				}
			}
			if (multiplayer) {
				try {
					Client.getOutToServerStream().writeObject(yeah.getPrototype());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else options.getBehaviour().onActionSelected(yeah.getPrototype());
		}
		
	}

}