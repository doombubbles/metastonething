package net.demilich.metastone.gui.multiplayermode;

import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.Server;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.gameconfig.MultiplayerConfig;
import net.demilich.metastone.gui.playmode.PlayModeMediator;
import net.demilich.nittygrittymvc.Mediator;
import net.demilich.nittygrittymvc.interfaces.INotification;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerConfigMediator extends Mediator<GameNotification> {

	public static final String NAME = "MultiplayerConfigMediator";

	private final MultiplayerModeConfigView view;

	private Thread server;
	private Thread client;

	public MultiplayerConfigMediator() {
		super(NAME);
		view = new MultiplayerModeConfigView();
	}

	@Override
	public void handleNotification(final INotification<GameNotification> notification) {
		switch (notification.getId()) {
		case REPLY_DECKS:
			@SuppressWarnings("unchecked")
			List<Deck> decks = (List<Deck>) notification.getBody();
			view.injectDecks(decks);
			break;
		case REPLY_DECK_FORMATS:
			@SuppressWarnings("unchecked")
			List<DeckFormat> deckFormats = (List<DeckFormat>) notification.getBody();
			view.injectDeckFormats(deckFormats);
			break;
		case COMMIT_MULTIPLAYER_CONFIG:
			MultiplayerConfig multiplayerConfig = (MultiplayerConfig) notification.getBody();
			if (multiplayerConfig.getIpAddress().equals("") || multiplayerConfig.getIpAddress().equals(null)) {
				server = new Thread(new Runnable() {
					@Override
					public void run() {
						Server.initialize(multiplayerConfig, BuildConfig.VERSION, false);
					}
				});
				server.setDaemon(true);
				server.start();
				multiplayerConfig.setIpAddress("localhost");
			}
			PlayModeMediator mediator = new PlayModeMediator(true);
			getFacade().registerMediator(mediator);
			client = new Thread(new Runnable() {
				@Override
				public void run() {
					Client.initialize(multiplayerConfig, BuildConfig.VERSION);
				}
			});
			client.setDaemon(true);
			client.start();
			break;
		case MAIN_MENU:
			if (server != null) {
				server.interrupt();
			}
			if (client != null) {
				client.interrupt();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public List<GameNotification> listNotificationInterests() {
		List<GameNotification> notificationInterests = new ArrayList<GameNotification>();
		notificationInterests.add(GameNotification.REPLY_DECKS);
		notificationInterests.add(GameNotification.REPLY_DECK_FORMATS);
		notificationInterests.add(GameNotification.COMMIT_MULTIPLAYER_CONFIG);
		notificationInterests.add(GameNotification.MAIN_MENU);
		return notificationInterests;
	}

	@Override
	public void onRegister() {
		getFacade().sendNotification(GameNotification.SHOW_VIEW, view);
		getFacade().sendNotification(GameNotification.REQUEST_DECKS);
		getFacade().sendNotification(GameNotification.REQUEST_DECK_FORMATS);
	}

}
