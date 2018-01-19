package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.PermanentCard;
import net.demilich.metastone.game.cards.RiftCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.minions.Permanent;
import net.demilich.metastone.game.entities.minions.Rift;
import net.demilich.metastone.game.targeting.CardReference;

public class PlayRiftCardAction extends PlayPermanentCardAction {

    public PlayRiftCardAction(CardReference cardReference) {
        super(cardReference);
        setActionType(ActionType.SUMMON);
    }

    @Override
    protected void play(GameContext context, int playerId, GameContext previousContext) {
        RiftCard riftCard = (RiftCard) context.getPendingCard();
        Actor nextTo = (Actor) (getTargetKey() != null ? context.resolveSingleTarget(getTargetKey()) : null);
        Rift rift = riftCard.summon();
        Player player = context.getPlayer(playerId);
        int index = player.getSummons().indexOf(nextTo);
        context.getLogic().summon(playerId, rift, riftCard, index, true);
    }
}
