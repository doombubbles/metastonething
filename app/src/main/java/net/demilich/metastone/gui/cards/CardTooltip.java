package net.demilich.metastone.gui.cards;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;

import java.util.regex.Pattern;

public class CardTooltip extends CardToken {

	@FXML
	private Label raceLabel;

	public CardTooltip() {
		super("CardTooltip.fxml");
	}

	@Override
	public void setCard(GameContext context, Card card, Player player) {
		super.setCard(context, card, player);

		if (!card.hasAttribute(Attribute.RACE) || card.getAttribute(Attribute.RACE) == Race.NONE) {
			raceLabel.setVisible(false);
		} else {
			raceLabel.setText(card.getAttribute(Attribute.RACE).toString());
			raceLabel.setVisible(true);
		}

		String descriptionText = card.getDescription();
		if (card.getDescValues().size() > 0 && context != null) {

			if (descriptionText.contains("[")) {
				descriptionText = descriptionText.replace(descriptionText.substring(descriptionText.indexOf("["), descriptionText.lastIndexOf("]") + 1), "");
			}
			if (descriptionText.contains("{")) {
				String partInCurlies = descriptionText.substring(descriptionText.indexOf("{"), descriptionText.lastIndexOf("}") + 1);
				String newPartInCurlies = partInCurlies;
				for (int i = 1; i <= card.getDescValues().size(); i++) {
					ValueProvider valueProvider = card.getDescValues().get(i-1);
					newPartInCurlies = newPartInCurlies.replace("$" + i, valueProvider.getValue(context, player, null, card) + "");
				}
				descriptionText = descriptionText.replace(partInCurlies, newPartInCurlies).replace("{", "").replace("}", "");
			}
		} else {
			if (descriptionText.contains("{")) {
				descriptionText = descriptionText.replace(descriptionText.substring(descriptionText.indexOf("{"), descriptionText.lastIndexOf("}") + 1), "");
			}
			if (descriptionText.contains("[")) {
				descriptionText = descriptionText.replace("[", "").replace("]", "");
			}
		}
		if (descriptionText.contains("*") && context != null) {
			String[] partTwixtStars = descriptionText.split(Pattern.quote("*"));
			for (int i = 0; i < partTwixtStars.length; i++) {
				String s = partTwixtStars[i];
				if (i % 2 == 1) {
					int oldValue = Integer.parseInt(s);
					int newValue = context.getLogic().applyAmplify(player, context.getLogic().applySpellpower(player, card, oldValue)
							+ card.getAttributeValue(Attribute.SPELL_DAMAGE)
							+ player.getAttributeValue(Attribute.SPELL_DAMAGE), Attribute.SPELLS_DAMAGE_MULT);

					descriptionText = descriptionText.replace("*" + s + "*","*" + newValue + "*");
					if (newValue <= oldValue) {
						descriptionText = descriptionText.replaceAll(Pattern.quote("*"), "");
					}
				}
			}
		} else {
			if (descriptionText.contains("*")) {
				descriptionText = descriptionText.replaceAll(Pattern.quote("*"), "");
			}
		}


		descriptionLabel.setText(descriptionText);

	}

}
