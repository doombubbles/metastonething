package net.demilich.metastone.gui.playmode.animation;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.CardPlayedEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.HeroPowerUsedEvent;
import net.demilich.metastone.gui.playmode.GameBoardView;

public class HeroPowerVisualizer implements IGameEventVisualizer {

	@Override
	public void visualizeEvent(GameContext gameContext, GameEvent event, GameBoardView boardView) {
		HeroPowerUsedEvent hPEvent = (HeroPowerUsedEvent) event;
		new CardPlayedToken(boardView, gameContext.getCardById(hPEvent.getHeroPower().getCardId()));
	}

}
