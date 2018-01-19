package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.SecretCard;
import net.demilich.metastone.game.events.SecretPlayedEvent;
import net.demilich.metastone.game.events.SecretRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.types.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class PlaySpellCardAction extends PlayCardAction {

	private SpellDesc spell;
	protected final EntityReference cardReference;

	public PlaySpellCardAction(SpellDesc spell, Card card, TargetSelection targetSelection) {
		super(card.getCardReference());
		setActionType(ActionType.SPELL);
		setTargetRequirement(targetSelection);
		this.setSpell(spell);
		this.cardReference = card.getReference();
	}

	@Override
	public void play(GameContext context, int playerId, GameContext previousContext) {
		Card card = (Card) context.resolveSingleTarget(cardReference);
		TargetSelection targetRequirement = getTargetRequirement();
		if (card instanceof SecretCard && context.getLogic().hasAttribute(context.getPlayer(playerId), Attribute.INSTANT_TRAPS)) {
			spell = ((SecretCard) card).getInstant().spell;
			targetRequirement = ((SecretCard) card).getInstant().getTargetSelection() != null ? ((SecretCard) card).getInstant().getTargetSelection() : TargetSelection.NONE;
			context.fireGameEvent(new SecretRevealedEvent(context, ((SecretCard) card), playerId));
			context.fireGameEvent(new SecretPlayedEvent(context, playerId, (SecretCard) card));
		}
		context.getLogic().castSpell(playerId, spell, cardReference, getTargetKey(), targetRequirement, false, previousContext);
	}

	public SpellDesc getSpell() {
		return spell;
	}

	public void setSpell(SpellDesc spell) {
		this.spell = spell;
	}


}
