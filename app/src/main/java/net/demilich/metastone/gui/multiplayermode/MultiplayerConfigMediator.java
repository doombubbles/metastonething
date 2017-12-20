package net.demilich.metastone.gui.multiplayermode;

import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.gameconfig.GameConfig;
import net.demilich.metastone.game.gameconfig.MultiplayerConfig;
import net.demilich.metastone.gui.playmode.PlayModeMediator;
import net.demilich.nittygrittymvc.Mediator;
import net.demilich.nittygrittymvc.interfaces.INotification;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerConfigMediator extends Mediator<GameNotification> {

	public static final String NAME = "MultiplayerConfigMediator";

	private final MultiplayerModeConfigView view;

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
				Server server = new Server(multiplayerConfig, BuildConfig.VERSION);
				MultiplayerMediator multiplayerMediator = new MultiplayerMediator(server);
				getFacade().registerMediator(multiplayerMediator);
				new Thread(new Runnable() {
					@Override
					public void run() {
						server.initialize();
					}
				}).start();
				multiplayerConfig.setIpAddress("localhost");
			}

			Client client = new Client(multiplayerConfig, BuildConfig.VERSION);
			PlayModeMediator mediator = new PlayModeMediator(client);
			getFacade().registerMediator(mediator);
			new Thread(new Runnable() {
				@Override
				public void run() {
					client.initialize();
				}
			}).start();


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
		return notificationInterests;
	}

	@Override
	public void onRegister() {
		getFacade().sendNotification(GameNotification.SHOW_VIEW, view);
		getFacade().sendNotification(GameNotification.REQUEST_DECKS);
		getFacade().sendNotification(GameNotification.REQUEST_DECK_FORMATS);
	}

}
