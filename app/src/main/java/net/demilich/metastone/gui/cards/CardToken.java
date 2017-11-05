package net.demilich.metastone.gui.cards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.ReplaceHeroCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.FilterArg;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.gui.DigitFactory;
import net.demilich.metastone.gui.IconFactory;

public class CardToken extends BorderPane {

	@FXML
	protected Group manaCostAnchor;
	@FXML
	protected Label nameLabel;
	@FXML
	protected Label descriptionLabel;

	@FXML
	protected Group attackAnchor;
	@FXML
	protected Group hpAnchor;

	@FXML
	protected ImageView attackIcon;
	@FXML
	protected ImageView hpIcon;

	@FXML
	protected Circle rarityGem;

	private double baseRarityGemSize;

	protected Card card;

	protected CardToken(String fxml) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		baseRarityGemSize = rarityGem.getRadius();
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		setCard(null, card, null);
	}

	public void setCard(GameContext context, Card card, Player player) {
		this.card = card;
		nameLabel.setText(card.getName());
		setRarity(card.getRarity());
		setHeroClass(card.getHeroClass());
		if (context != null || player != null) {
			int modifiedManaCost = context.getLogic().getModifiedManaCost(player, card);
			setScoreValueLowerIsBetter(manaCostAnchor, modifiedManaCost, card.getBaseManaCost());
		} else {
			setScoreValue(manaCostAnchor, card.getBaseManaCost());
		}

		boolean isMinionOrWeaponCard = card.getCardType().isCardType(CardType.MINION) || card.getCardType().isCardType(CardType.WEAPON);
		attackAnchor.setVisible(isMinionOrWeaponCard);
		hpAnchor.setVisible(isMinionOrWeaponCard || card.getCardType().isCardType(CardType.REPLACE_HERO));
		attackIcon.setVisible(isMinionOrWeaponCard);
		hpIcon.setVisible(isMinionOrWeaponCard || card.getCardType().isCardType(CardType.REPLACE_HERO));
		if (card.getCardType().isCardType(CardType.MINION)) {
			MinionCard minionCard = (MinionCard) card;
			setScoreValue(attackAnchor, minionCard.getAttack() + minionCard.getBonusAttack(), minionCard.getBaseAttack());
			setScoreValue(hpAnchor, minionCard.getHp() + minionCard.getBonusHp(), minionCard.getBaseHp());
		} else if (card.getCardType().isCardType(CardType.WEAPON)) {
			WeaponCard weaponCard = (WeaponCard) card;
			setScoreValue(attackAnchor, weaponCard.getDamage() + weaponCard.getBonusDamage(), weaponCard.getBaseDamage());
			setScoreValue(hpAnchor, weaponCard.getDurability() + weaponCard.getBonusDurability(), weaponCard.getBaseDurability());
		} else if (card.getCardType().isCardType(CardType.REPLACE_HERO)) {
			ReplaceHeroCard replaceHeroCard = (ReplaceHeroCard) card;
			setScoreValue(hpAnchor, replaceHeroCard.armor);
		}
	}

	public void setHeroClass(HeroClass heroClass) {
		nameLabel.getStyleClass().add("thickBorder");
		nameLabel.getStyleClass().remove("mage");
		nameLabel.getStyleClass().remove("paladin");
		nameLabel.getStyleClass().remove("priest");
		nameLabel.getStyleClass().remove("druid");
		nameLabel.getStyleClass().remove("hunter");
		nameLabel.getStyleClass().remove("shaman");
		nameLabel.getStyleClass().remove("deathKnight");
		nameLabel.getStyleClass().remove("rogue");
		nameLabel.getStyleClass().remove("warlock");
		nameLabel.getStyleClass().remove("warrior");
		switch (heroClass) {
			case DRUID:
				nameLabel.getStyleClass().add("druid");
				break;
			case MAGE:
				nameLabel.getStyleClass().add("mage");
				break;
			case PALADIN:
				nameLabel.getStyleClass().add("paladin");
				break;
			case PRIEST:
				nameLabel.getStyleClass().add("priest");
				break;
			case HUNTER:
				nameLabel.getStyleClass().add("hunter");
				break;
			case ROGUE:
				nameLabel.getStyleClass().add("rogue");
				break;
			case WARLOCK:
				nameLabel.getStyleClass().add("warlock");
				break;
			case WARRIOR:
				nameLabel.getStyleClass().add("warrior");
				break;
			case SHAMAN:
				nameLabel.getStyleClass().add("shaman");
				break;
			case DEATH_KNIGHT:
				nameLabel.getStyleClass().add("deathKnight");
				break;
			default:
				break;

		}

	}

	public void setNonCard(String name, String description) {
		nameLabel.setText(name);
		descriptionLabel.setText(description);
		setRarity(Rarity.FREE);
		manaCostAnchor.setVisible(false);
		attackAnchor.setVisible(false);
		hpAnchor.setVisible(false);
		attackIcon.setVisible(false);
		hpIcon.setVisible(false);
	}

	private void setRarity(Rarity rarity) {
		rarityGem.setFill(IconFactory.getRarityColor(rarity));
		rarityGem.setVisible(rarity != Rarity.FREE);
		rarityGem.setRadius(rarity == Rarity.LEGENDARY ? baseRarityGemSize * 1.25 : baseRarityGemSize);
	}

	protected void setScoreValue(Group group, int value) {
		setScoreValue(group, value, value);
	}

	protected void setScoreValue(Group group, int value, int baseValue) {
		Color color = Color.WHITE;
		if (value > baseValue) {
			color = Color.GREEN;
		}
		DigitFactory.showPreRenderedDigits(group, value, color);
	}
	
	protected void setScoreValue(Group group, int value, int baseValue, int maxValue) {
		Color color = Color.WHITE;
		if (value < maxValue) {
			color = Color.RED;
		} else if (value > baseValue) {
			color = Color.GREEN;
		}
		DigitFactory.showPreRenderedDigits(group, value, color);
	}

	private void setScoreValueLowerIsBetter(Group group, int value, int baseValue) {
		Color color = Color.WHITE;
		if (value < baseValue) {
			color = Color.GREEN;
		} else if (value > baseValue) {
			color = Color.RED;
		}
		DigitFactory.showPreRenderedDigits(group, value, color);
	}
	
	

}
