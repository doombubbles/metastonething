package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;

public class HeroClassCondition extends Condition {
    public HeroClassCondition(ConditionDesc desc) {
        super(desc);
    }

    @Override
    protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
        HeroClass heroClass = (HeroClass) desc.get(ConditionArg.HERO_CLASS);
        TargetPlayer targetPlayer = (TargetPlayer) desc.get(ConditionArg.TARGET_PLAYER);
        switch (targetPlayer) {
        case ACTIVE:
            return context.getActivePlayer().getHero().getHeroClass().equals(heroClass);
        case INACTIVE:
            return context.getOpponent(context.getActivePlayer()).getHero().getHeroClass().equals(heroClass);
        case BOTH:
            return true;
        case OPPONENT:
            return context.getOpponent(player).getHero().getHeroClass().equals(heroClass);
        case SELF:
            return player.getHero().getHeroClass().equals(heroClass);
        default:
            return player.getHero().getHeroClass().equals(heroClass);
        }
    }
}
