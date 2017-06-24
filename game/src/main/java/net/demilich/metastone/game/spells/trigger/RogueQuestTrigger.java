package net.demilich.metastone.game.spells.trigger;

import java.util.Arrays;

import javax.smartcardio.Card;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.AfterSpellCastedEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.SpellCastedEvent;
import net.demilich.metastone.game.events.SummonEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class RogueQuestTrigger extends GameEventTrigger {

	public RogueQuestTrigger(EventTriggerDesc desc) {
		super(desc);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		SummonEvent summonEvent = (SummonEvent) event;
		if (summonEvent.getSource() == null) {
			return false;
		}
		
		Minion minion = (Minion) summonEvent.getMinion();

		Player player = (Player) summonEvent.getGameContext().getActivePlayer();
		
		
		int i;
		
		
		if (!player.roguequest.isEmpty()) {
			
			
			Object[] array = player.roguequest.values().toArray();
			Arrays.sort(array);
			i = (int) array[array.length - 1];
		} else i = 0;
		
		if (player.roguequest.get(minion.getName()) != null) {
			player.roguequest.put(minion.getName(), player.roguequest.get(minion.getName()) + 1);
		} else player.roguequest.put(minion.getName(), 1);
		
		Object[] array2 = player.roguequest.values().toArray();
		int i2;
		if (array2.length > 0) {
			Arrays.sort(array2);
			i2 = (int) array2[array2.length - 1];
		} else i2 = 0;
		
		if (i2 > i) {
			return true;
		} else return false;
		
	
		
		
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.SUMMON;
	}
	
}
