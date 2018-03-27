package net.demilich.metastone.game.actions;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.cards.ReplaceHeroCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.events.HeroPowerChangedEvent;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class PlayReplaceHeroCardAction extends PlayCardAction {

	private final BattlecryAction battlecry;
	
	public PlayReplaceHeroCardAction(CardReference cardReference, BattlecryAction battlecry) {
		super(cardReference);
		this.battlecry = battlecry;
		setActionType(ActionType.REPLACE_HERO);
	}

	@Override
	protected void play(GameContext context, int playerId, GameContext previousContext) {
		Player player = context.getPlayer(playerId);
		ReplaceHeroCard replaceHeroCard = (ReplaceHeroCard) context.getPendingCard();
		HeroCard heroCard = (HeroCard) context.getCardById(replaceHeroCard.hero).clone();
		heroCard.setAttribute(Attribute.HP, context.getPlayer(playerId).getHero().getHp());
		heroCard.setAttribute(Attribute.ARMOR, player.getHero().getArmor() + replaceHeroCard.armor);
		Hero hero = heroCard.createHero();
		context.fireGameEvent(new HeroPowerChangedEvent(context, playerId, hero.getHeroPower()));
		context.getLogic().changeHero(player, hero);
		
		Actor actor = (Actor) player.getHero();
		
		BattlecryAction battlecry = this.battlecry;
		GameAction battlecryAction = null;
		battlecry.setSource(actor.getReference());
		if (battlecry.getTargetRequirement() != TargetSelection.NONE) {
			List<Entity> validTargets = context.getLogic().getValidTargets(playerId, (GameAction) battlecry);
			if (validTargets.isEmpty()) {
				return;
			}

			List<GameAction> battlecryActions = new ArrayList<>();
			for (Entity validTarget : validTargets) {
				GameAction targetedBattlecry = battlecry.clone();
				targetedBattlecry.setTarget(validTarget);
				battlecryActions.add(targetedBattlecry);
			}
			
			
			battlecryAction = player.getBehaviour().requestAction(context, player, battlecryActions);
			
		} else {
			battlecryAction = battlecry;
		}
		if (context.getLogic().hasAttribute(player, Attribute.DOUBLE_BATTLECRIES) && actor.getSourceCard().hasAttribute(Attribute.BATTLECRY)) {
			context.getLogic().performGameAction(playerId, battlecryAction);
			if (!battlecry.canBeExecuted(context, player)) {
				return;
			}
			context.getLogic().performGameAction(playerId, battlecryAction);
		} else {
			context.getLogic().performGameAction(playerId, battlecryAction);
		}
		
	}

}
