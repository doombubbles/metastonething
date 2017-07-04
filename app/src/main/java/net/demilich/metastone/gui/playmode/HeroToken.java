package net.demilich.metastone.gui.playmode;

import java.util.HashSet;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import jdk.nashorn.internal.runtime.Context;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.QuestCard;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.gui.IconFactory;
import net.demilich.metastone.gui.cards.CardTooltip;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.GameContext;

public class HeroToken extends GameToken {

	@FXML
	private Group attackAnchor;
	@FXML
	private Group hpAnchor;
	@FXML
	private Label cardsLabel;
	@FXML
	private Label manaLabel;
	
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
	private Pane secretsAnchor;

	@FXML
	private Shape frozen;
	
	@FXML
	private ImageView immune;

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
		Hero hero = player.getHero();
		setScoreValue(attackAnchor, hero.getAttack());
		Image portraitImage = new Image(IconFactory.getHeroIconUrl(hero.getSourceCard().getCardId()));
		portrait.setImage(portraitImage);
		setScoreValue(hpAnchor, hero.getHp(), hero.getAttributeValue(Attribute.BASE_HP), hero.getMaxHp());
		if (!player.getDeck().isEmpty()) {
			cardsLabel.setText("Cards in deck: " + player.getDeck().getCount());
		} else {
			cardsLabel.setText("Fatigue: " + player.getAttributeValue(Attribute.FATIGUE));
		}
		
		
		if (!player.getQuests().isEmpty()) {
			int n = 0;
			switch ((String) player.getQuests().toArray()[player.getQuests().size() - 1]) {
				case "quest_awaken_the_makers":
				case "quest_fire_plumes_heart":
				case "quest_the_marsh_queen":
					n = 7;
					break;
				case "quest_unite_the_murlocs":
					n = 10;
					break;
				case "quest_jungle_giants":
				case "quest_the_last_kaleidosaur":
				case "quest_the_caverns_below":
				case "quest_lakkari_sacrifice":
					n = 5;
					break;
				case "quest_open_the_waygate":
					n = 6;
					break;
			}
			questLabel.setText("Quest: " + (n - player.getAttributeValue(Attribute.QUEST)) + "/" + n);
			
			
		} else questLabel.setText("            "); 
		
		
		
		
		if (player.getAttributeValue(Attribute.OVERLOAD) > 0) {
			manaLabel.setText("Mana: " + player.getMana() + "/" + player.getMaxMana() + "\nOver: " + player.getAttributeValue(Attribute.OVERLOAD));
		} else {
			manaLabel.setText("Mana: " + player.getMana() + "/" + player.getMaxMana());
		}
		updateArmor(hero.getArmor());
		updateHeroPower(hero);
		updateWeapon(hero.getWeapon());
		updateSecrets(player);
		updateStatus(hero, context);
	}

	private void updateArmor(int armor) {
		setScoreValue(armorAnchor, armor);
		boolean visible = armor > 0;
		armorIcon.setVisible(visible);
		armorAnchor.setVisible(visible);
	}

	private void updateHeroPower(Hero hero) {
		Image heroPowerImage = new Image(IconFactory.getHeroPowerIconUrl(hero.getHeroPower()));
		heroPowerIcon.setImage(heroPowerImage);
		Card card = CardCatalogue.getCardById(hero.getHeroPower().getCardId());
		Tooltip tooltip = new Tooltip();
		CardTooltip tooltipContent = new CardTooltip();
		tooltipContent.setCard(card);
		tooltip.setGraphic(tooltipContent);
		Tooltip.install(heroPowerIcon, tooltip);
		
		Image portraitImage = new Image(IconFactory.getHeroIconUrl(hero.getSourceCard().getCardId()));
		portrait.setImage(portraitImage);
	}

	public void updateHeroPowerCost(GameContext context, Player player) {
		setScoreValueLowerIsBetter(heroPowerAnchor, context.getLogic().getModifiedManaCost(player, player.getHero().getHeroPower()), player.getHero().getHeroPower().getBaseManaCost());
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
				} else secretIcon = new ImageView(IconFactory.getImageUrl("common/secret.png"));
			}
			secretsAnchor.getChildren().add(secretIcon);

			if (!player.hideCards() || card instanceof QuestCard) {
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
		immune.setVisible(hero.hasAttribute(Attribute.IMMUNE) || hasAttribute(context.getPlayer(hero.getOwner()), Attribute.IMMUNE_HERO, context));
	}
	
	public boolean hasAttribute(Player player, Attribute attr, GameContext context) {
		return context.getLogic().hasAttribute(player, attr);
	}

	private void updateWeapon(Weapon weapon) {
		boolean hasWeapon = weapon != null;
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
		}
	}

}