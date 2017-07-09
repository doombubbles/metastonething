package net.demilich.metastone.game.spells;

import java.util.Map;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;

public class SwitchHeroSpell extends Spell {

	public static SpellDesc create(String heroCardId) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SwitchHeroSpell.class);
		arguments.put(SpellArg.CARD, heroCardId);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Hero hero = player.getHero();
		String heroCardId = (String) desc.get(SpellArg.CARD);
		HeroCard heroCard = (HeroCard) context.getCardById(heroCardId).clone();
		heroCard.setAttribute(Attribute.HP, hero.getHp());
		context.getLogic().changeHero(player, heroCard.createHero());
	}

}
