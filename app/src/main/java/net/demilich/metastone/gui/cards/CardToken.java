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
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
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
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.FilterArg;
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
		if (context != null || player != null) {
			int modifiedManaCost = context.getLogic().getModifiedManaCost(player, card);
			setScoreValueLowerIsBetter(manaCostAnchor, modifiedManaCost, card.getBaseManaCost());
			evaluateGlow(context, card, player);
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
		rarityGem.setRadius(rarity == Rarity.LEGENDARY ? baseRarityGemSize * 1.5 : baseRarityGemSize);
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
		String bad = "spell_execute, spell_shadow_word_pain, spell_shadow_word_death, spell_potion_of_madness, spell_shadow_madness, spell_flame_cannon, spell_breath_of_sindragosa, "
				+ "minion_don_hancho, minion_grimestreet_smuggler, minion_shaky_zipgunner";
		if (context.getLogic().canPlayCard(player.getId(), card.getCardReference()) && context.getActivePlayer() == player) {
			glow("GREEN");
			if (bad.contains(card.getCardId())) {
				return;
			}
			if (card.getCardType() == CardType.MINION) {
				MinionCard minion = (MinionCard) card;
				if (minion.summon().getBattlecry() != null) {
					BattlecryAction battlecry = BattlecryAction.createBattlecry(minion.summon().getBattlecry().getSpell(), minion.summon().getBattlecry().getTargetRequirement());
					battlecry.setSource(card.getReference());
					if (minion.summon().getBattlecry().getCondition() != null) {
						Condition condition = minion.summon().getBattlecry().getCondition();
						if (minion.summon().getBattlecry().getTargetRequirement() == TargetSelection.NONE) {
							if (condition.isFulfilled(context, player, (Entity) player, null)) {
								glow("YELLOW");
							}
						} else {
							List<Entity> validTargets = context.getLogic().getValidTargets(player.getId(), battlecry);
							if (!validTargets.isEmpty()) {
								int i = 0;
								for (Entity entity : validTargets) {
									i += condition.isFulfilled(context, player, context.resolveSingleTarget(card.getReference()), entity) ? 1 : 0;
								}
								if (i > 0) {
									glow("YELLOW");
								}
							}
						}
					}
					if (minion.summon().getBattlecry().getEntityFilter() != null) {
						EntityFilter filter = minion.summon().getBattlecry().getEntityFilter();
						if (minion.summon().getBattlecry().getTargetRequirement() == TargetSelection.NONE) {
							if (filter.matches(context, player, minion)) {
								glow("YELLOW");
							}
						} else {
							List<Entity> validTargets = context.getLogic().getValidTargets(player.getId(), battlecry);
							if (!validTargets.isEmpty()) {
								int i = 0;
								for (Entity entity : validTargets) {
									i += filter.matches(context, player, entity) ? 1 : 0;
								}
								if (i > 0) {
									glow("YELLOW");
								}
							}
						}
					}
					
					
				}
			} else if (card.getCardType() == CardType.SPELL) {
				SpellCard spell = (SpellCard) card;
				BattlecryAction battlecry = BattlecryAction.createBattlecry(spell.getSpell(), spell.getTargetRequirement());
				battlecry.setSource(card.getReference());
				if (spell.hasAttribute(Attribute.COMBO) && player.hasAttribute(Attribute.COMBO)) {
					glow("YELLOW");
				}
				if (spell.getSpell().getEntityFilter() != null) {
					EntityFilter filter = spell.getSpell().getEntityFilter();
					if (spell.getTargetRequirement() == TargetSelection.NONE) {
						
					} else {
						List<Entity> validTargets = context.getLogic().getValidTargets(player.getId(), battlecry);
						if (!validTargets.isEmpty()) {
							int i = 0;
							for (Entity entity : validTargets) {
								i += filter.matches(context, player, entity) ? 1 : 0;
							}
							if (i > 0) {
								glow("YELLOW");
							} else glow("NOPE");
						} 
					}
				}
			} else if (card.getCardType() == CardType.WEAPON) {
				WeaponCard weapon = (WeaponCard) card;
				if (weapon.hasBattlecry()) {
					BattlecryAction battlecry = BattlecryAction.createBattlecry(weapon.getWeapon().getBattlecry().getSpell(), weapon.getWeapon().getBattlecry().getTargetRequirement());
					battlecry.setSource(card.getReference());
					if (weapon.getWeapon().getBattlecry().getCondition() != null) {
						Condition condition = weapon.getWeapon().getBattlecry().getCondition();
						if (weapon.getWeapon().getBattlecry().getTargetRequirement() == TargetSelection.NONE) {
							if (condition.isFulfilled(context, player, (Entity) player, null)) {
								glow("YELLOW");
							}
						} else {
							List<Entity> validTargets = context.getLogic().getValidTargets(player.getId(), battlecry);
							if (!validTargets.isEmpty()) {
								int i = 0;
								for (Entity entity : validTargets) {
									i += condition.isFulfilled(context, player, context.resolveSingleTarget(card.getReference()), entity) ? 1 : 0;
								}
								if (i > 0) {
									glow("YELLOW");
								}
							}
						}
					}
				}
			} 
		}
	}

}
