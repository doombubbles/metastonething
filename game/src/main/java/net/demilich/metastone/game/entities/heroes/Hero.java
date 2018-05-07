package net.demilich.metastone.game.entities.heroes;

import java.util.EnumMap;
import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.heroes.powers.HeroPower;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.AndCondition;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.HeroPowerChangedTrigger;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;

public class Hero extends Actor {

	private HeroClass heroClass;
	private HeroPower heroPower;
	private Weapon weapon;
	private HeroPower heroPower2;

	public Hero(HeroCard heroCard, HeroPower heroPower) {
		super(heroCard);
		setName(heroCard.getName());
		this.setHeroClass(heroCard.getHeroClass());
		this.setHeroPower(heroPower);
	}

	public void activateWeapon(boolean active) {
		if (weapon != null) {
			weapon.setActive(active);
		}
	}

	@Override
	public Hero clone() {
		Hero clone = (Hero) super.clone();
		if (weapon != null) {
			clone.setWeapon(getWeapon().clone());
		}
		
		clone.setHeroPower((HeroPower) getHeroPower().clone());
		return clone;
	}

	public int getArmor() {
		return getAttributeValue(Attribute.ARMOR);
	}

	@Override
	public int getAttack() {
		int attack = super.getAttack();
		if (weapon != null && weapon.isActive()) {
			attack += weapon.getWeaponDamage();
		}
		return attack;
	}

	public Map<Attribute, Object> getAttributesCopy() {
		Map<Attribute, Object> copy = new EnumMap<>(Attribute.class);
		for (Attribute attribute : attributes.keySet()) {
			copy.put(attribute, attributes.get(attribute));
		}
		return copy;
	}

	public int getEffectiveHp() {
		return getHp() + getArmor();
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.HERO;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public HeroPower getHeroPower() {
		return heroPower;
	}

	public HeroPower getHeroPower2() {
		return heroPower2;
	}

	public boolean hasHeroPower2() {
		return heroPower2 != null;
	}

	public void removeHeroPower2() {
		heroPower2 = null;
	}

	public Weapon getWeapon() {
		return weapon;
	}

	public void modifyArmor(int armor) {
		// armor cannot fall below zero
		int newArmor = Math.max(getArmor() + armor, 0);
		setAttribute(Attribute.ARMOR, newArmor);
	}
	
	public void setHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public void setHeroPower(HeroPower heroPower) {
		setHeroPower(heroPower, 1);
	}

	public void setHeroPower(HeroPower heroPower, int power) {
		if (heroPower.hasTrigger()) {
			SpellTrigger trigger = heroPower.getTrigger().create();
			Map<EventTriggerArg, Object> eventTriggerMap = EventTriggerDesc.build(HeroPowerChangedTrigger.class);
			eventTriggerMap.put(EventTriggerArg.TARGET_PLAYER, TargetPlayer.SELF);
			trigger.addRevertTrigger(new EventTriggerDesc(eventTriggerMap).create());
			trigger.heroPower = true;
			addSpellTrigger(trigger);
		}
		switch (power) {
			case 2:
				this.heroPower2 = heroPower;
				break;
			default:
				this.heroPower = heroPower;
		}
		heroPower.setOwner(getOwner());
	}

	@Override
	public void setOwner(int ownerIndex) {
		super.setOwner(ownerIndex);
		heroPower.setOwner(ownerIndex);
	}

	public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
		if (weapon != null) {
			weapon.setOwner(getOwner());
		}
	}

}
