package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.SecretCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.DeckSource;
import net.demilich.metastone.game.spells.desc.source.SourceDesc;
import net.demilich.metastone.game.spells.trigger.types.Secret;
import net.demilich.metastone.game.targeting.CardLocation;

public class PutRandomSecretIntoPlaySpell extends Spell {

	private CardList findSecretCards(CardList cardCollection) {
		CardList secretCards = new CardList();
		for (Card card : cardCollection) {
			if (card.hasAttribute(Attribute.SECRET)) {
				secretCards.add(card);
			}
		}
		return secretCards;
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		if (cardSource == null) {
			cardSource = new SourceDesc(SourceDesc.build(DeckSource.class)).create();
		}
		EntityFilter filter = desc.getEntityFilter();

		for (int i = 0; i < howMany; i++) {
			CardList secretCards = findSecretCards(cardSource.getCards(context, player));

			if (secretCards.isEmpty()) {
				return;
			}
			
			secretCards.shuffle();

			SecretCard secretCard = (SecretCard) secretCards.removeFirst();
			while (!secretCards.isEmpty()) {
				if (!context.getLogic().canPlaySecret(player, secretCard)
						&& (filter == null || filter.matches(context, player, secretCard, source))) {
					secretCard = (SecretCard) secretCards.removeFirst();
				} else {
					break;
				}
			}
			if (secretCards.isEmpty() && !context.getLogic().canPlaySecret(player, secretCard)) {
				return;
			}
			SpellDesc secretSpellDesc = secretCard.getSpell();
			Secret secret = (Secret) secretSpellDesc.get(SpellArg.SECRET);
			context.getLogic().playSecret(player, secret, false);
			if (secretCard.getLocation() != CardLocation.DECK) {
				context.getLogic().removeCardFromHand(player.getId(), secretCard);
			} else {
				context.getLogic().removeCardFromDeck(player.getId(), secretCard);
			}

		}
	}

}