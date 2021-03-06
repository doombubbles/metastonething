package net.demilich.metastone.game.spells.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.spells.Spell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.utils.IDisposable;

public class TriggerManager implements Cloneable, IDisposable, Serializable {

	public static Logger logger = LoggerFactory.getLogger(TriggerManager.class);

	private final List<IGameEventListener> triggers = new ArrayList<IGameEventListener>();

	public TriggerManager() {
	}

	private TriggerManager(TriggerManager otherTriggerManager) {
		for (IGameEventListener gameEventListener : otherTriggerManager.triggers) {
			triggers.add(gameEventListener.clone());
		}
	}

	public void addTrigger(IGameEventListener trigger) {
		triggers.add(trigger);
		if (triggers.size() > 100) {
			logger.warn("Warning, many triggers: " + triggers.size() + " adding one of type: " + trigger);
		}
	}

	@Override
	public TriggerManager clone() {
		return new TriggerManager(this);
	}

	@Override
	public void dispose() {
		triggers.clear();
	}

	public void fireGameEvent(GameEvent event) {
		if (event.getEventTarget() != null) {
			event.getGameContext().getEventTargetStack().push(event.getEventTarget().getReference());
		} else {
			event.getGameContext().getEventTargetStack().push(null);
		}
		if (event.getEventSource() != null) {
			event.getGameContext().getEventSourceStack().push(event.getEventSource().getReference());
			//System.out.println("EVENT_SOURCE IS NOW " + event.getEventSource().getName());
		} else {
			event.getGameContext().getEventSourceStack().push(null);
		}
		List<IGameEventListener> eventTriggers = new ArrayList<IGameEventListener>();
		List<IGameEventListener> removeTriggers = new ArrayList<IGameEventListener>();
		for (IGameEventListener trigger : triggers) {
			// In order to stop premature expiration, check
			// for a oneTurnOnly tag and that it isn't delayed.

			if (event.after) {
				try {
					boolean hey = false;
					for (IGameEventListener potentialTrigger : event.getPreviousContext().getTriggers()) {
						if (potentialTrigger.getOwner() == trigger.getOwner() && potentialTrigger.getHostReference() == trigger.getHostReference() && potentialTrigger.getClass() == trigger.getClass()) {
							hey = true;
						}
					}
					if (!hey) {
						continue;
					}
				} catch (Exception e) {
					continue;
				}

			}

			if (event.getEventType() == GameEventType.TURN_END) {
				if(trigger.oneTurnOnly() && !trigger.isDelayed() &&
						!trigger.interestedIn(GameEventType.TURN_START) &&
						!trigger.interestedIn(GameEventType.TURN_END)) {
					trigger.expire();
				}
				if (trigger.doesReset()) {
					trigger.resetCount();
				}
				trigger.delayTimeDown();
			}
			if (trigger.revertInterestedIn(event.getEventType())) {
				trigger.expire();
			}
			if (trigger.isExpired()) {
				removeTriggers.add(trigger);
			}

			if (!trigger.interestedIn(event.getEventType())) {
				continue;
			}
			if (triggers.contains(trigger) && trigger.canFire(event)) {
				eventTriggers.add(trigger);
			}
		}

		for (IGameEventListener trigger : eventTriggers) {

			if (trigger.canFireCondition(event) && triggers.contains(trigger)) {
				trigger.countDown(event);
				if (!trigger.hasCounter()) {
					if (trigger instanceof SpellTrigger) {
						SpellTrigger spellTrigger = (SpellTrigger) trigger;
						if (!(spellTrigger.heroPower && event.getGameContext().getLogic().attributeExists(Attribute.KRYPTONITE))) {
							trigger.onGameEvent(event);
						}
					} else trigger.onGameEvent(event);
				}
			}

			// we need to double check here if the trigger still exists;
			// after all, a previous trigger may have removed it (i.e. double
			// corruption)
			if (trigger.isExpired()) {
				removeTriggers.add(trigger);
			}
			
			if (trigger.oneTimeOnly()) {
				removeTriggers.add(trigger);
			}
		}
		
		
		
		for (IGameEventListener trigger : removeTriggers) {
			triggers.remove(trigger);
		}

		event.getGameContext().getEventTargetStack().pop();
		event.getGameContext().getEventSourceStack().pop();
	}

	private List<IGameEventListener> getListSnapshot(List<IGameEventListener> triggerList) {
		return new ArrayList<IGameEventListener>(triggerList);
	}

	public List<IGameEventListener> getTriggersAssociatedWith(EntityReference entityReference) {
		List<IGameEventListener> relevantTriggers = new ArrayList<>();
		for (IGameEventListener trigger : triggers) {
			if (trigger.getHostReference().equals(entityReference)) {
				relevantTriggers.add(trigger);
			}
		}
		return relevantTriggers;
	}

	public List<IGameEventListener> getTriggers() {
		return triggers;
	}

	public void printCurrentTriggers() {
		for (IGameEventListener trigger : triggers) {
			System.out.println();
			System.out.println(trigger.toString());
			System.out.println();
		}
	}

	public void removeTrigger(IGameEventListener trigger) {
		if (!triggers.remove(trigger)) {
			System.out.println("Failed to remove trigger " + trigger);
		}
	}

	public void removeTriggersAssociatedWith(EntityReference entityReference, boolean removeAuras) {
		for (IGameEventListener trigger : getListSnapshot(triggers)) {
			if (trigger.getHostReference().equals(entityReference)) {
				if (!removeAuras && trigger instanceof Aura) {
					continue;
				}
				triggers.remove(trigger);
			}
		}
	}

}
