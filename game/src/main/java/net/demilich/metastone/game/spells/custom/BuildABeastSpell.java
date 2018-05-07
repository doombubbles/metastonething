package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class BuildABeastSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards1 = new CardList();
		CardList cards2 = new CardList();
		for (Card card : CardCatalogue.query(context.getDeckFormat())) {
			if (card.getCardType() == CardType.MINION && card.getRace() == Race.BEAST && card.getBaseManaCost() <= 5 && (card.hasHeroClass(HeroClass.HUNTER) || card.hasHeroClass(HeroClass.ANY))) {
				MinionCard beast = (MinionCard) card;
				if (!beast.hasAttribute(Attribute.BATTLECRY) && !beast.hasAttribute(Attribute.DEATHRATTLES) && beast.summon().getSpellTriggers().isEmpty() && beast.summon().getCardCostModifier() == null) {
					cards2.add(card);
				} else cards1.add(card);
			}
		}
		
		
		
		CardList results1 = new CardList();
		CardList results2 = new CardList();
		
		for (int i = 0; i < 3; i++) {
			if (!cards1.isEmpty()) {
				Card card = cards1.getRandom();
				results1.add(card);
				cards1.remove(card);
			}
			if (!cards2.isEmpty()) {
				Card card = cards2.getRandom();
				results2.add(card);
				cards2.remove(card);
			}
		}
		
		MinionCard card1 = (MinionCard) SpellUtils.getDiscover(context, player, desc, results1).getCard();
		MinionCard card2 = (MinionCard) SpellUtils.getDiscover(context, player, desc, results2).getCard();
		
		MinionCard card3 = (MinionCard) card1.clone();
		card3.setName("Zombeast");
		for (Attribute attribute : card2.getAttributes().keySet()) {
			card3.setAttribute(attribute, card2.getAttributes().get(attribute));
		}
		card3.setAttribute(Attribute.BASE_HP, (card1.getAttributeValue(Attribute.BASE_HP) + card2.getAttributeValue(Attribute.BASE_HP)));
		card3.setAttribute(Attribute.BASE_ATTACK, (card1.getAttributeValue(Attribute.BASE_ATTACK) + card2.getAttributeValue(Attribute.BASE_ATTACK)));
		card3.setAttribute(Attribute.HP, (card1.getAttributeValue(Attribute.HP) + card2.getAttributeValue(Attribute.HP)));
		card3.setAttribute(Attribute.ATTACK, (card1.getAttributeValue(Attribute.ATTACK) + card2.getAttributeValue(Attribute.ATTACK)));
		card3.setAttribute(Attribute.BASE_MANA_COST, (card1.getAttributeValue(Attribute.BASE_MANA_COST) + card2.getAttributeValue(Attribute.BASE_MANA_COST)));
		card3.setDescription(card2.getDescription() + " " + card1.getDescription());
		context.getLogic().receiveCard(player.getId(), card3);

		context.addTempCard(card3);
	}

}
