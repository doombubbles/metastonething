package net.demilich.metastone.game.events;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class TurnEndEvent extends GameEvent {

	public TurnEndEvent(GameContext context, int playerId) {
		super(context, playerId, -1);
		
		Player player = context.getPlayer(playerId);
		if (player.getAttribute(Attribute.ELEMENTALS_THIS_TURN) != null) {
			player.setAttribute(Attribute.ELEMENTALS_LAST_TURN, player.getAttributeValue(Attribute.ELEMENTALS_THIS_TURN));
			player.setAttribute(Attribute.ELEMENTALS_THIS_TURN, 0);
		}
	}
	
	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.TURN_END;
	}

}
