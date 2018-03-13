package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class FurnacefireSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int attack = 0;
		int hp = 0;
		CardList weapons = new CardList();
		for (Card card : player.getHand()) {
			if (card.getCardType() == CardType.WEAPON) {
				weapons.add(card);
			}
		}
		for (Card card : weapons) {
			WeaponCard weapon = (WeaponCard) card;
			attack += weapon.getBaseDamage() + weapon.getBonusDamage();
			hp += weapon.getBaseDurability() + weapon.getBonusDurability();
			context.getLogic().discardCard(player, card);
		}
		SpellDesc buffSpell = BuffSpell.create(target.getReference(), attack, hp);
		SpellUtils.castChildSpell(context, player, buffSpell, source, target);
		
	}

}
