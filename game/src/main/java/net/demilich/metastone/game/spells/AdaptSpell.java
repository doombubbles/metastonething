package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

public class AdaptSpell extends Spell {
	
	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = SpellDesc.build(DiscoverCardSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);  
		return new SpellDesc(arguments);
	}

	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		// no target specified, cast the spell once with target NULL
		if (targets == null) {
			return;
		}

		EntityFilter targetFilter = desc.getEntityFilter();
		List<Entity> validTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter);
		Entity randomTarget = null;
		if (validTargets.size() > 0 && desc.getBool(SpellArg.RANDOM_TARGET)) {
			randomTarget = SpellUtils.getRandomTarget(validTargets);
		}
		SpellDesc[] group = SpellUtils.getGroup(context, desc);
		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, null, source, 3);
		int count = desc.getValue(SpellArg.VALUE, context, player, null, source, 1);
		List<SpellDesc> spellList = new ArrayList<SpellDesc>();
		for (SpellDesc spell : group) {
			CardSet set = (CardSet) spell.get(SpellArg.OBJECT);
			Condition condition = (Condition) spell.get(SpellArg.CONDITION);

			if (set != null) {
				if (!context.getDeckFormat().getCardSets().contains(set)) {
					continue;
				}
				spell = spell.removeArg(SpellArg.OBJECT);
			}
			if (condition != null) {
				int i = 0;
				for (Entity validTarget : validTargets) {
					if (condition.isFulfilled(context, player, source, validTarget)) {
						i++;
					}
				}
				if (i == 0) {
					continue;
				}
				spell = spell.removeArg(SpellArg.CONDITION);
			}

			spellList.add(spell);
		}

		for (int j = 0; j < count; j++) {
			List<SpellDesc> adaptions = new ArrayList<SpellDesc>(spellList);
			List<SpellDesc> spells = new ArrayList<SpellDesc>();
			for (int i = 0; i < howMany; i++) {
				SpellDesc spell = adaptions.remove(context.getLogic().random(adaptions.size()));
				spells.add(spell);

				if (context.getLogic().hasAttribute(player, Attribute.ALL_OPTIONS)) {
					if (validTargets.size() > 0 && desc.getBool(SpellArg.RANDOM_TARGET)) {
						onCast(context, player, spell, source, randomTarget);
					} else {
						// there is at least one target and RANDOM_TARGET flag is not set,
						// cast in on all targets

						for (Entity target : validTargets) {
							context.getEnvironment().put(Environment.SPELL_TARGET, target.getReference());
							onCast(context, player, spell, source, target);
							context.getEnvironment().remove(Environment.SPELL_TARGET);
						}
					}
				}
			}
			
			if (spells.isEmpty() || context.getLogic().hasAttribute(player, Attribute.ALL_OPTIONS)) {
				continue;
			}
			SpellDesc spell = SpellUtils.getSpellDiscover(context, player, desc, spells).getSpell();
			spellList.remove(spell);
			
			if (validTargets.size() > 0 && desc.getBool(SpellArg.RANDOM_TARGET)) {
				onCast(context, player, spell, source, randomTarget);
			} else {
				// there is at least one target and RANDOM_TARGET flag is not set,
				// cast in on all targets

				for (Entity target : validTargets) {
					context.getEnvironment().put(Environment.SPELL_TARGET, target.getReference());
					onCast(context, player, spell, source, target);
					context.getEnvironment().remove(Environment.SPELL_TARGET);
				}
			}
		}
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellUtils.castChildSpell(context, player, desc, source, target);
	}

}
