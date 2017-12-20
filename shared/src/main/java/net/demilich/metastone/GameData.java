package net.demilich.metastone;

import net.demilich.metastone.GameNotification;

import java.io.Serializable;

public class GameData implements Serializable {
    private final GameNotification GAME_NOTIFICATION;
    private final Object DATA;

    public GameData (GameNotification gameNotification, Object data){
        this.GAME_NOTIFICATION = gameNotification;
        this.DATA = data;
    }

    public GameNotification getGameNotification() {
        return GAME_NOTIFICATION;
    }

    public Object getData() {
        return DATA;
    }
}
