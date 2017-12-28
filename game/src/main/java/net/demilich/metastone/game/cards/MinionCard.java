package net.demilich.metastone.game.cards;

import java.util.*;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayMinionCardAction;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;

public class MinionCard extends SummonCard {

	private static final Set<Attribute> ignoredAttributes = new HashSet<Attribute>(
			Arrays.asList(new Attribute[] { Attribute.PASSIVE_TRIGGER, Attribute.DECK_TRIGGER, Attribute.MANA_COST_MODIFIER, Attribute.BASE_ATTACK,
					Attribute.BASE_HP, Attribute.SECRET, Attribute.QUEST, Attribute.CHOOSE_ONE, Attribute.BATTLECRY, Attribute.COMBO }));

	private final MinionCardDesc desc;

	public MinionCard(MinionCardDesc desc) {
		super(desc);
		setAttribute(Attribute.BASE_ATTACK, desc.baseAttack);
		setAttribute(Attribute.ATTACK, desc.baseAttack);
		setAttribute(Attribute.BASE_HP, desc.baseHp);
		setAttribute(Attribute.HP, desc.baseHp);
		setAttribute(Attribute.MAX_HP, desc.baseHp);
		if (desc.race != null) {
			setRace(desc.race);
		}
		this.desc = desc;
	}

	protected Minion createMinion(Attribute... tags) {
		Minion minion = new Minion(this);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				minion.setAttribute(gameTag, getAttribute(gameTag));
			}
		}
		minion.setBaseAttack(getBaseAttack());
		minion.setAttack(getAttack());
		minion.setHp(getHp());
		minion.setMaxHp(getHp());
		minion.setBaseHp(getBaseHp());
		BattlecryDesc battlecry = desc.battlecry;
		if (battlecry != null) {
			BattlecryAction battlecryAction = BattlecryAction.createBattlecry(battlecry.spell, battlecry.getTargetSelection());
			if (battlecry.condition != null) {
				battlecryAction.setCondition(battlecry.condition.create());
			}
			minion.setBattlecry(battlecryAction);

		}
		if (desc.deathrattle != null) {
			minion.removeAttribute(Attribute.DEATHRATTLES);
			minion.addDeathrattle(desc.deathrattle);
		}
		if (desc.trigger != null) {
			minion.addSpellTrigger(desc.trigger.create());
		}
		if (desc.triggers != null) {
			for (TriggerDesc trigger : desc.triggers) {
				minion.addSpellTrigger(trigger.create());
			}
		}
		if (desc.aura != null) {
			minion.addSpellTrigger(desc.aura.create());
			minion.setAttribute(Attribute.AURA, true);
		}
		if (desc.cardCostModifier != null) {
			minion.setCardCostModifier(desc.cardCostModifier.create());
		}
		minion.setHp(minion.getMaxHp());
		return minion;
	}

	public int getAttack() {
		return getAttributeValue(Attribute.ATTACK);
	}
	
	public int getBonusAttack() {
		return getAttributeValue(Attribute.ATTACK_BONUS);
	}

	public int getHp() {
		return getAttributeValue(Attribute.HP);
	}
	
	public int getBonusHp() {
		return getAttributeValue(Attribute.HP_BONUS);
	}

	public int getBaseAttack() {
		return getAttributeValue(Attribute.BASE_ATTACK);
	}

	public int getBaseHp() {
		return getAttributeValue(Attribute.BASE_HP);
	}

	@Override
	public PlayCardAction play() {
		return new PlayMinionCardAction(getCardReference());
	}

	public void setRace(Race race) {
		setAttribute(Attribute.RACE, race);
	}

	public Minion summon() {
		return createMinion();
	}

	public boolean hasDeathrattle() {
		return desc.deathrattle != null;
	}

	public SpellDesc getDeathrattle() {
		return  desc.deathrattle;
	}

	public TriggerDesc getTrigger() {
		return desc.trigger;
	}

	public void setTrigger(TriggerDesc trigger) {
		desc.trigger = trigger;
	}

	public TriggerDesc[] getTriggers() {
		return desc.triggers;
	}

	public void setTriggers(TriggerDesc[] triggers) {
		desc.triggers = triggers;
	}

	public CardCostModifierDesc getCostModifier() {
		return desc.cardCostModifier;
	}

	public void setCostModifier(CardCostModifierDesc costModifier) {
		desc.cardCostModifier = costModifier;
	}

	public AuraDesc getAura() {
		return desc.aura;
	}

	public void setAura(AuraDesc aura) {
		desc.aura = aura;
	}

	@Override
	public void setBattlecry(BattlecryDesc battlecry) {
		desc.battlecry = battlecry;
	}

	@Override
	public BattlecryDesc getBattlecry() {
		return desc.battlecry;
	}

	public void addDeathrattle(SpellDesc deathrattleSpell) {
		SpellDesc combinedDeathrattle;
		if (desc.deathrattle != null) {
			combinedDeathrattle = MetaSpell.create(deathrattleSpell, desc.deathrattle);
		} else combinedDeathrattle = deathrattleSpell;

		desc.deathrattle = combinedDeathrattle;
	}

	public MinionCardDesc getDesc() {
		return desc;
	}

}
