package net.demilich.metastone.gui.multiplayermode;

import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.behaviour.human.HumanActionOptions;
import net.demilich.metastone.game.behaviour.human.HumanMulliganOptions;
import net.demilich.metastone.gui.playmode.GameContextVisualizable;
import net.demilich.nittygrittymvc.Mediator;
import net.demilich.nittygrittymvc.interfaces.INotification;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerMediator extends Mediator<GameNotification>{

    private static final String NAME = "MultiplayerMediator";

    private final Server server;

    public MultiplayerMediator(Server server) {
        super(NAME);
        this.server = server;
    }

    @Override
    public void handleNotification(final INotification<GameNotification> notification) {
        switch (notification.getId()) {
            case SERVER_PROMPT_FOR_MULLIGAN:
                HumanMulliganOptions options = (HumanMulliganOptions) notification.getBody();
                int playerId = options.getPlayer().getId();
                if (playerId == 1) {
                    options.switchPlayers();
                }
                server.sendNotification(GameNotification.HUMAN_PROMPT_FOR_MULLIGAN, options, playerId);
                break;
            case SERVER_PROMPT_FOR_ACTION:
                HumanActionOptions actionOptions = (HumanActionOptions) notification.getBody();
                int player = actionOptions.getPlayer().getId();
                if (player == 1) {
                    actionOptions.switchPlayers();
                }
                server.sendNotification(GameNotification.HUMAN_PROMPT_FOR_ACTION, actionOptions, player);
                break;
            case SERVER_GAME_STATE_UPDATE:
                GameContextVisualizable context = (GameContextVisualizable) notification.getBody();
                server.sendNotification(GameNotification.GAME_STATE_UPDATE, context, 0);
                server.sendNotification(GameNotification.GAME_STATE_UPDATE, context.cloneAndSwitch(), 1);
                break;
            case SERVER_GAME_STATE_LATE_UPDATE:
                GameContextVisualizable context2 = (GameContextVisualizable) notification.getBody();
                server.sendNotification(GameNotification.GAME_STATE_LATE_UPDATE, context2, 0);
                server.sendNotification(GameNotification.GAME_STATE_LATE_UPDATE, context2.cloneAndSwitch(), 1);
                break;
            default:
                break;
        }
    }

    @Override
    public List<GameNotification> listNotificationInterests() {
        List<GameNotification> notificationInterests = new ArrayList<GameNotification>();
        notificationInterests.add(GameNotification.SERVER_PROMPT_FOR_MULLIGAN);
        notificationInterests.add(GameNotification.SERVER_PROMPT_FOR_ACTION);
        notificationInterests.add(GameNotification.SERVER_GAME_STATE_UPDATE);
        notificationInterests.add(GameNotification.SERVER_GAME_STATE_LATE_UPDATE);

        return notificationInterests;
    }
}
