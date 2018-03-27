package net.demilich.metastone.gui;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.heroes.powers.HeroPower;
import net.demilich.metastone.gui.dialog.DialogType;

public class IconFactory {

	//public static final String RESOURCE_PATH = "/net/demilich/metastone/resources";
	public static final String RESOURCE_PATH = "";

	public static Image getClassIcon(HeroClass heroClass) {
		String iconPath = RESOURCE_PATH + "/img/classes/";
		iconPath += heroClass.toString().toLowerCase();
		iconPath += ".png";
		return new Image(iconPath);
	}

	public static Image getDefaultCardBack() {
		String iconPath = RESOURCE_PATH + "/img/common/card_back_default.png";
		return new Image(iconPath);
	}

	public static Image getDialogIcon(DialogType dialogType) {
		String iconPath = RESOURCE_PATH + "/img/ui/";
		switch (dialogType) {
		case CONFIRM:
			iconPath += "confirm.png";
			break;
		case ERROR:
			iconPath += "error.png";
			break;
		case INFO:
			iconPath += "info.png";
			break;
		case WARNING:
			iconPath += "warning.png";
			break;
		default:
			break;

		}
		return new Image(iconPath);
	}
	
	public static String getHeroIconUrl(Card heroCard) {
		String iconPath = RESOURCE_PATH + "/img/heroes/";
		String hero;
		if (heroCard != null) {
			if (heroCard.getCardSet() == CardSet.CUSTOM) {
				iconPath = RESOURCE_PATH + "/img/heroes/custom/";
			}
			hero = heroCard.getCardId().replace("hero_", "");
		} else hero = "unknown";

		try {
			new Image(iconPath + hero + ".png");
		} catch (Exception e) {
			hero = "unknown";
		}
		
		return iconPath + hero + ".png";
	}
	
	public static String getHeroPowerIconUrl(HeroPower heroPower) {
		String iconPath = RESOURCE_PATH + "/img/powers/";
		if (heroPower.getCardSet() == CardSet.CUSTOM) {
			iconPath = RESOURCE_PATH + "/img/powers/custom/";
		}
		
		String power = heroPower.getCardId().replace("hero_power_", "");

		try {
			new Image(iconPath + power + ".png");
		} catch (Exception e) {
			power = "unknown";
		}

		return iconPath + power + ".png";
	}
	
	
	
	public static String getImageUrl(String imageName) {
		//System.out.println(new File("").getAbsolutePath());
		return RESOURCE_PATH + "/img/" + imageName;
	}

	public static Color getRarityColor(Rarity rarity) {
		Color color = Color.BLACK;
		switch (rarity) {
		case COMMON:
			color = Color.WHITE;
			break;
		case EPIC:
			// a335ee
			color = Color.rgb(163, 53, 238);
			break;
		case LEGENDARY:
			// ff8000
			color = Color.rgb(255, 128, 0);
			break;
		case RARE:
			// 0070dd
			color = Color.rgb(0, 112, 221);
			break;
		default:
			color = Color.GRAY;
			break;
		}
		return color;

	}

	public static Image getSummonHelper() {
		String iconPath = RESOURCE_PATH + "/img/common/arrow_down_blue.png";
		return new Image(iconPath);
	}

	public static Image getTargetIcon() {
		String iconPath = RESOURCE_PATH + "/img/common/target.png";
		return new Image(iconPath);
	}

	private IconFactory() {
	}
}