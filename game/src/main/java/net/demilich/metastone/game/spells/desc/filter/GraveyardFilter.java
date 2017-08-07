package net.demilich.metastone.game.spells.desc.filter;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;

public class GraveyardFilter extends EntityFilter {

	public GraveyardFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity) {
		Card card = null;
		
		List<Minion> deadMinions = new ArrayList<>();
		List<Entity> graveyard = new ArrayList<Entity>();
		graveyard.addAll(player.getGraveyard());
		for (Entity deadEntity : graveyard) {
			if (deadEntity.getEntityType() == EntityType.MINION) {
				deadMinions.add((Minion) deadEntity);
			}
		}
		
		if (entity.getEntityType().equals(EntityType.CARD)) {
			card = (Card) entity;
		}
		for (Minion minion : deadMinions) {
			if (minion.getSourceCard().getCardId().equals(card.getCardId())) {
				return true;
			}
			
		}
		return false;
	}

}
