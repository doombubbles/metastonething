package net.demilich.metastone.gui.playmode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.PermanentCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Permanent;
import net.demilich.metastone.game.entities.minions.Summon;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;
import net.demilich.metastone.gui.cards.CardTooltip;

public class SummonToken extends GameToken {
	@FXML
	private Label name;
	@FXML
	private Group attackAnchor;
	@FXML
	private Group hpAnchor;

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
	private Node immune;
	
	@FXML
	private Shape elusivetaunt;

	private CardTooltip cardTooltip;
	
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
			hpAnchor.setVisible(true);
			setScoreValue(attackAnchor, summon.getAttack(), summon.getAttributeValue(Attribute.BASE_ATTACK));
			setScoreValue(hpAnchor, summon.getHp(), summon.getBaseHp(), summon.getMaxHp());
		} else if (summon instanceof Permanent) {
			attackAnchor.setVisible(false);
			hpAnchor.setVisible(false);
		}
		visualizeStatus(summon);
		cardTooltip.setCard(summon.getSourceCard());
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
		//trigger.setVisible(summon.hasSpellTrigger() && !summon.hasAttribute(Attribute.AURA) && !summon.hasAttribute(Attribute.HIDE_TRIGGER));
		int i = 0;
		for (SpellTrigger spelltrig : summon.getSpellTriggers()) {
			if (!spelltrig.interestedIn(GameEventType.ALL) && !spelltrig.interestedIn(GameEventType.ENRAGE_CHANGED) && !spelltrig.interestedIn(GameEventType.BOARD_CHANGED)) {
				i += 1;
			}
		}
		trigger.setVisible(i > 0);
		
		elusive.setVisible((summon.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS) || summon.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)) && !summon.hasAttribute(Attribute.TAUNT));
		elusivetaunt.setVisible((summon.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS) || summon.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS))  && summon.hasAttribute(Attribute.TAUNT));
		visualizeStealth(summon);
		spell_damage.setVisible(summon.hasAttribute(Attribute.SPELL_DAMAGE));
		
		
	}

	private void visualizeStealth(Summon summon) {
		Node token = summon.hasAttribute(Attribute.TAUNT) ? taunt : defaultToken;
		token.setOpacity(summon.hasAttribute(Attribute.STEALTH) ? 0.5 : 1);
	}

}
