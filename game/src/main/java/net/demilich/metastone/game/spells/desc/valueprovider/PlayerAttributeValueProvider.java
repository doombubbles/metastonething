package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.PlayerAttribute;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.statistics.Statistic;

public class PlayerAttributeValueProvider extends ValueProvider {

	public PlayerAttributeValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		PlayerAttribute attribute = (PlayerAttribute) desc.get(ValueProviderArg.PLAYER_ATTRIBUTE);
		switch (attribute) {
		case DECK_COUNT:
			return player.getDeck().getCount();
		case HAND_COUNT:
			CardCollection cards = player.getHand();
			if (desc.contains(ValueProviderArg.FILTER)) {
				EntityFilter filter = (EntityFilter) desc.get(ValueProviderArg.FILTER);
				cards.removeAll(card -> !filter.matches(context, player, card));
			}
			return cards.getCount();
		case HERO_POWER_USED:
			return (int) player.getStatistics().getLong(Statistic.HERO_POWER_USED);
		case LAST_MANA_COST:
			return (int) context.getEnvironment().get(Environment.LAST_MANA_COST);
		case MANA:
			return player.getMana();
		case MAX_MANA:
			return player.getMaxMana();
		case SECRET_COUNT:
			return player.getSecrets().size() - player.getQuests().size();
		case SPELLS_CAST:
			return (int) player.getStatistics().getLong(Statistic.SPELLS_CAST);
		case OVERLOADED_MANA:
			return (int) player.getStatistics().getLong(Statistic.OVERLOADED_MANA);
		case DAMAGE_THIS_TURN:
			return  (int) player.getStatistics().getLong(Statistic.DAMAGE_THIS_TURN);
		default:
			break;
		}
		return 0;
	}

}
