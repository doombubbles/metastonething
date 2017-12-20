package net.demilich.metastone.game.entities.minions;

import net.demilich.metastone.game.cards.RiftCard;

public class Rift extends Permanent {

    private int totalDuration;
    private int time;

    public Rift(RiftCard sourceCard, int duration) {
        super(sourceCard);
        totalDuration = duration;
        time = duration;
    }

    public int countDown(int amount) {
        time -= amount;
        return time;
    }

    public int countDown() {
        return countDown(1);
    }

    public int getTime() {
        return time;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

}
