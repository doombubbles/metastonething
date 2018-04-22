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
import net.demilich.metastone.game.events.BoardChangedEvent;
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
		ReplaceHeroCard replaceHeroCard = (ReplaceHeroCard) context.getPendingCard();
		context.getLogic().replaceHero(playerId, replaceHeroCard,battlecry);
	}

}
