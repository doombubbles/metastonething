package net.demilich.metastone.gui.multiplayermode;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.human.HumanActionOptions;
import net.demilich.metastone.game.behaviour.human.HumanMulliganOptions;
import net.demilich.metastone.game.behaviour.human.HumanTargetOptions;
import net.demilich.metastone.gui.playmode.HumanActionPromptView;
import net.demilich.metastone.gui.playmode.HumanMulliganView;
import net.demilich.metastone.gui.playmode.PlayModeView;
import net.demilich.nittygrittymvc.Mediator;
import net.demilich.nittygrittymvc.interfaces.INotification;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerMediator extends Mediator<GameNotification> implements EventHandler<KeyEvent> {

    private static final String NAME = "MultiplayerMediator";

    private final PlayModeView view;
    private final HumanActionPromptView actionPromptView;

    public MultiplayerMediator() {
        super(NAME);
        view = new PlayModeView();
        actionPromptView = view.getActionPromptView();
    }

    @Override
    public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() != KeyCode.ESCAPE) {
            return;
        }

        view.disableTargetSelection();
    }

    public void thing(HumanActionOptions actionOptions) {
        actionPromptView.setActions(actionOptions, true);
        view.doCardActionStuff(actionOptions);
    }

    @Override
    public void handleNotification(final INotification<GameNotification> notification) {
        switch (notification.getId()) {
            case GAME_STATE_UPDATE:
                GameContext context = (GameContext) notification.getBody();
                Platform.runLater(() -> view.showAnimations(context));
                break;
            case GAME_STATE_LATE_UPDATE:
                GameContext context2 = (GameContext) notification.getBody();
                Platform.runLater(() -> view.updateGameState(context2));
                break;
            case HUMAN_PROMPT_FOR_ACTION:
                HumanActionOptions actionOptions = (HumanActionOptions) notification.getBody();
                Platform.runLater(() -> thing(actionOptions));
                break;
            case HUMAN_PROMPT_FOR_TARGET:
                HumanTargetOptions options = (HumanTargetOptions) notification.getBody();
                Platform.runLater(() -> view.enableTargetSelection(options));
                break;
            case HUMAN_PROMPT_FOR_MULLIGAN:
                HumanMulliganOptions mulliganOptions = (HumanMulliganOptions) notification.getBody();
                Platform.runLater(() -> new HumanMulliganView(mulliganOptions));
                break;
            case HIDE_ACTIONS:
                HumanActionOptions actionOptions2 = (HumanActionOptions) notification.getBody();
                Platform.runLater(() -> actionPromptView.setActions(actionOptions2, false));
                break;
            default:
                break;
        }
    }

    @Override
    public List<GameNotification> listNotificationInterests() {
        List<GameNotification> notificationInterests = new ArrayList<GameNotification>();
        notificationInterests.add(GameNotification.GAME_STATE_UPDATE);
        notificationInterests.add(GameNotification.GAME_STATE_LATE_UPDATE);
        notificationInterests.add(GameNotification.HUMAN_PROMPT_FOR_ACTION);
        notificationInterests.add(GameNotification.HUMAN_PROMPT_FOR_TARGET);
        notificationInterests.add(GameNotification.HUMAN_PROMPT_FOR_MULLIGAN);
        notificationInterests.add(GameNotification.REPLY_DECKS);
        notificationInterests.add(GameNotification.REPLY_DECK_FORMATS);
        notificationInterests.add(GameNotification.HIDE_ACTIONS);
        return notificationInterests;
    }

    @Override
    public void onRegister() {
        getFacade().sendNotification(GameNotification.SHOW_VIEW, view);
        view.getScene().setOnKeyPressed(this);
    }
}
