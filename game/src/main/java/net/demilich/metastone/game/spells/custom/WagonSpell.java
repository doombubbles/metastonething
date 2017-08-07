package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.CardLocation;

public class WagonSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int attack = ((Minion) source).getAttack();
		CardCollection dirt = new CardCollection();
		for (Card card : player.getDeck()) {
			if (card.getAttributeValue(Attribute.ATTACK) < attack && card.getCardType() == CardType.MINION) {
				dirt.add(card);
			}
		}
		MinionCard minionCard = (MinionCard) dirt.getRandom();

		if (minionCard == null) {
			return;
		}
		
		player.getDeck().remove(minionCard);
		player.getSetAsideZone().add(minionCard);
		
		boolean summonSuccess = context.getLogic().summon(player.getId(), minionCard.summon());
		
		
		player.getSetAsideZone().remove(minionCard);
		player.getDeck().add(minionCard);
		
		
		if (summonSuccess) {
			context.getLogic().removeCardFromDeck(player.getId(), minionCard);
			
		}
		
	}

}
