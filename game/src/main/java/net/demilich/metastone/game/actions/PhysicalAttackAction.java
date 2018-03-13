package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class PhysicalAttackAction extends GameAction {

	private final EntityReference attackerReference;

	public PhysicalAttackAction(EntityReference attackerReference) {
		setTargetRequirement(TargetSelection.ENEMY_CHARACTERS);
		setActionType(ActionType.PHYSICAL_ATTACK);
		this.attackerReference = attackerReference;
	}
	
	@Override
	public boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		if (!super.canBeExecutedOn(context, player, entity)) {
			return false;
		}
		if (entity.getEntityType() != EntityType.HERO) {
			return true;
		}
		Actor attacker = (Actor) context.resolveSingleTarget(attackerReference);
		if (attacker.hasAttribute(Attribute.CANNOT_ATTACK_HEROES) ||
				((attacker.hasAttribute(Attribute.CANNOT_ATTACK_HERO_ON_SUMMON) || attacker.hasAttribute(Attribute.RUSH))
						&& attacker.hasAttribute(Attribute.SUMMONING_SICKNESS))) {
			return false;
		}
		if (attacker.getEntityType() == EntityType.HERO) {
			if (((Hero) attacker).getWeapon() != null) {
				if (((Hero) attacker).getWeapon().hasAttribute(Attribute.CANNOT_ATTACK_HEROES)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void execute(GameContext context, int playerId) {
		Actor defender = (Actor) context.resolveSingleTarget(getTargetKey());
		Actor attacker = (Actor) context.resolveSingleTarget(attackerReference);

		context.getLogic().fight(context.getPlayer(playerId), attacker, defender);
	}

	public EntityReference getAttackerReference() {
		return attackerReference;
	}

	@Override
	public String getPromptText() {
		return "[Physical Attack]";
	}

	@Override
	public boolean isSameActionGroup(GameAction anotherAction) {
		if (anotherAction.getActionType() != getActionType()) {
			return false;
		}
		PhysicalAttackAction physicalAttackAction = (PhysicalAttackAction) anotherAction;

		return this.getAttackerReference().equals(physicalAttackAction.getAttackerReference());
	}

	@Override
	public String toString() {
		return String.format("%s Attacker: %s Defender: %s", getActionType(), attackerReference, getTargetKey());
	}

}
