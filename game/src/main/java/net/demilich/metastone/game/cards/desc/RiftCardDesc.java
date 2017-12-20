package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.PermanentCard;
import net.demilich.metastone.game.cards.RiftCard;

public class RiftCardDesc extends PermanentCardDesc {

    public int duration;

    @Override
    public Card createInstance() {
        return new RiftCard(this);
    }
}
