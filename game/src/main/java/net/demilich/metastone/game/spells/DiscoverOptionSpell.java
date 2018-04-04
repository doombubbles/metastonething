package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class DiscoverOptionSpell extends Spell {
	
	Logger logger = LoggerFactory.getLogger(DiscoverOptionSpell.class);
	
	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = SpellDesc.build(DiscoverOptionSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);
		return new SpellDesc(arguments);
	}
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<SpellDesc> spells = new ArrayList<SpellDesc>();
		boolean reveal = desc.getBool(SpellArg.REVEAL);
		boolean random = desc.getBool(SpellArg.RANDOM_TARGET);
		SpellDesc[] spellArray = (SpellDesc[]) desc.get(SpellArg.SPELLS);
		for (SpellDesc spell : spellArray) {
			spells.add(spell);
		}
		
		Map<SpellDesc, Integer> spellOrder = new HashMap<SpellDesc, Integer>();
		for (int i = 0; i < spells.size(); i++)
		{
		    SpellDesc spell = spells.get(i);
		    spellOrder.put(spell, i);
		}
		
		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		boolean exclusive = desc.getBool(SpellArg.EXCLUSIVE);
		List<Integer> chosenSpellInts = new ArrayList<Integer>();
		for (int i = 0; i < value; i++) {
			List<SpellDesc> spellChoices = new ArrayList<SpellDesc>();
			List<SpellDesc> spellsCopy = new ArrayList<SpellDesc>(spells);
			for (int j = 0; j < count; j++) {
				if (!spellsCopy.isEmpty()) {
					SpellDesc spell = spellsCopy.get(random ? context.getLogic().random(spellsCopy.size()) : 0);
					spellChoices.add(spell);
					spellsCopy.remove(spell);
				}
			}
			if (!spellChoices.isEmpty()) {
				DiscoverAction action = SpellUtils.getSpellDiscover(context, player, desc, spellChoices);
				SpellDesc chosenSpell = action.getSpell();
				chosenSpellInts.add(spellOrder.get(chosenSpell));
				if (exclusive) {
					spellChoices.remove(chosenSpell);
					spells.remove(chosenSpell);
				}
				if (reveal) {
					context.fireGameEvent(new CardRevealedEvent(context, context.getPlayer1().getId(), CardCatalogue.getCardById(action.getName().replaceAll(" ", "_").toLowerCase()), (1 + i) * 1.2));
					context.fireGameEvent(new CardRevealedEvent(context, context.getPlayer2().getId(), CardCatalogue.getCardById(action.getName().replaceAll(" ", "_").toLowerCase()), (1 + i) * 1.2));
				}
			}
		}
		
		
		Collections.sort(chosenSpellInts);
		SpellDesc[] chosenSpells = new SpellDesc[chosenSpellInts.size()];
		for (int i = 0; i < chosenSpellInts.size(); i++) {
			chosenSpells[i] = spellArray[chosenSpellInts.get(i)];
		}
		if (chosenSpellInts.size() > 0) {
			SpellDesc metaSpell = MetaSpell.create(target != null ? target.getReference() : null, false, chosenSpells);
			SpellUtils.castChildSpell(context, player, metaSpell, source, target);
		}
		
		
		
		
	}

}
