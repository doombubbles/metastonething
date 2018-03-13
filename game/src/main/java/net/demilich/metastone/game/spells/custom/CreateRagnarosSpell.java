package net.demilich.metastone.game.spells.custom;

import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.CardCatalogue;

public class CreateRagnarosSpell extends Spell {
	
	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CreateRagnarosSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);  
		return new SpellDesc(arguments);
	}
	
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = new CardList();
		cards.add(CardCatalogue.getCardById("minion_base_lesser_rag"));
		cards.add(CardCatalogue.getCardById("minion_base_greater_rag"));
		cards.add(CardCatalogue.getCardById("minion_base_woke_rag"));
		
		Card chosenSize = SpellUtils.getDiscover(context, player, desc, cards).getCard();
		
		
		CardList cards2 = new CardList();
		Card chosenEffect = null;
		switch (chosenSize.getManaCost(context, player)) {
		case 3:
			cards2.add(CardCatalogue.getCardById("spell_base_lesser_die"));
			cards2.add(CardCatalogue.getCardById("spell_base_lesser_live"));
			cards2.add(CardCatalogue.getCardById("spell_base_lesser_smash"));
			
			chosenEffect = SpellUtils.getDiscover(context, player, desc, cards2).getCard();
			
			switch (chosenEffect.getManaCost(context, player)) {
			case 1:
				context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_lesser_die_rag"));
				break;
			case 2:
				context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_lesser_live_rag"));
				break;
			case 3:
				context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_lesser_smash_rag"));
				break;
			default:
				break;
			
			}
			break;
		case 5:
			cards2.add(CardCatalogue.getCardById("spell_base_greater_die"));
			cards2.add(CardCatalogue.getCardById("spell_base_greater_live"));
			cards2.add(CardCatalogue.getCardById("spell_base_greater_smash"));
			
			chosenEffect = SpellUtils.getDiscover(context, player, desc, cards2).getCard();
			
			switch (chosenEffect.getManaCost(context, player)) {
			case 4:
				context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_greater_die_rag"));
				break;
			case 5:
				context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_greater_live_rag"));
				break;
			case 6:
				context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_greater_smash_rag"));
				break;
			default:
				break;	
			}
			break;
		case 8:
			cards2.add(CardCatalogue.getCardById("spell_base_woke_die"));
			cards2.add(CardCatalogue.getCardById("spell_base_woke_live"));
			cards2.add(CardCatalogue.getCardById("spell_base_woke_smash"));
			
			chosenEffect = SpellUtils.getDiscover(context, player, desc, cards2).getCard();
			
			switch (chosenEffect.getManaCost(context, player)) {
			case 7:
				context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_woke_die_rag"));
				break;
			case 8:
				context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_woke_live_rag"));
				break;
			case 9:
				context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_woke_smash_rag"));
				break;
			default:
				break;	
			}
			break;
			/*
		default:
			context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_annoy-o-tron"));
			break;
			*/
		}

		return;
	}
}
