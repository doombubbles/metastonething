package net.demilich.metastone.gui.multiplayermode;

import net.demilich.metastone.GameData;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.human.ActionGroup;
import net.demilich.metastone.game.behaviour.human.HumanActionOptions;
import net.demilich.metastone.game.behaviour.human.HumanMulliganOptions;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.gameconfig.MultiplayerConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.gui.playmode.GameContextVisualizable;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {

    private final int PORT;
    private final String VERSION;
    private final MultiplayerConfig multiplayerConfig;
    private boolean inGame;

    private static ObjectOutputStream outToHostClientStream;
    private static ObjectInputStream inFromHostClientsStream;

    private static ObjectOutputStream outToConnectingClientStream;
    private static ObjectInputStream inFromConnectingClientStream;


    //public static ArrayList<Socket> connections = new ArrayList<Socket>();
    public Socket hostSocket;
    public Socket connectingSocket;

    public Server (MultiplayerConfig multiplayerConfig, String version) {
        this.multiplayerConfig = multiplayerConfig;
        this.PORT = multiplayerConfig.getPort();
        this.VERSION = version;
    }

    public static void main(String[] args){
        MultiplayerConfig config = new MultiplayerConfig();
        config.setPort(Integer.parseInt(args[0]));
        Server server = new Server(config, args[1]);
        server.initialize();
    }

    public void initialize(){
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Yay! We have a server socket!");
            hostSocket = serverSocket.accept();
            /*
            if (!VERSION.equals(handleVersion(hostSocket))) {
                throw new IOException("RIP");
            }
            */
            outToHostClientStream = new ObjectOutputStream(hostSocket.getOutputStream());
            inFromHostClientsStream = new ObjectInputStream(hostSocket.getInputStream());

            PlayerConfig playerConfig1 = (PlayerConfig) inFromHostClientsStream.readObject();


            connectingSocket = serverSocket.accept();
            /*
            if (!VERSION.equals(handleVersion(connectingSocket))) {
                throw new IOException("RIP2");
            }
            */
            outToConnectingClientStream = new ObjectOutputStream(connectingSocket.getOutputStream());
            inFromConnectingClientStream = new ObjectInputStream(connectingSocket.getInputStream());
            PlayerConfig playerConfig2 = (PlayerConfig) inFromConnectingClientStream.readObject();



            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        playGame(playerConfig1, playerConfig2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();

            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (inGame) {
                            System.out.println("spam0");
                            receiveNotification((GameData) inFromHostClientsStream.readObject(), 0);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            t2.setDaemon(true);
            t2.start();

            Thread t3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (inGame) {
                            System.out.println("spam1");
                            receiveNotification((GameData) inFromConnectingClientStream.readObject(), 1);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            t3.setDaemon(true);
            t3.start();




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playGame(PlayerConfig playerConfig1, PlayerConfig playerConfig2) throws IOException {
        inGame = true;

        Player player1 = new Player(playerConfig1);
        Player player2 = new Player(playerConfig2);
        DeckFormat deckFormat = multiplayerConfig.getDeckFormat();

        //outToHostClientStream.writeObject(new GameContextVisualizable(player1, player2, new GameLogic(), deckFormat, true));
        //outToHostClientStream.writeObject(new GameContextVisualizable(player2, player1, new GameLogic(), deckFormat, true));
        GameContext context = new GameContextVisualizable(player1, player2, new GameLogic(), deckFormat, true);

        synchronized (hostSocket) {
            hostSocket.notify(); //WAKE UP!
        }
        synchronized (connectingSocket) {
            connectingSocket.notify(); //WAKE UP!
        }
        context.play();
        inGame = false;
    }

    public static String handleVersion(Socket socket) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String version = bufferedReader.readLine();
            System.out.println("We got to the version part of it! " + version);
            return version;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
    @SuppressWarnings("unchecked")
    public void receiveNotification(GameData gameData, int playerId) {
        GameNotification notification = gameData.getGameNotification();
        Object object = gameData.getData();
        if (notification.equals(GameNotification.REPLY_FROM_SERVER_PROMPT_FOR_MULLIGAN)) {
            ArrayList<Object> data = (ArrayList<Object>) object;
            HumanMulliganOptions options = playerId == 1 ? ((HumanMulliganOptions) data.get(0)).switchPlayers() : (HumanMulliganOptions) data.get(0);
            List<Card> discardedCard = (List<Card>) data.get(1);
            System.out.println("receivey");
            options.getBehaviour().setMulliganCards(discardedCard);
        } else if (notification.equals(GameNotification.REPLY_FROM_SERVER_PROMPT_FOR_ACTION)) {
            ArrayList<Object> data = (ArrayList<Object>) object;
            HumanActionOptions options = (HumanActionOptions) data.get(0);
            GameAction action = (GameAction) data.get(1);
            options.switchPlayers().getBehaviour().onActionSelected(action);
        } else NotificationProxy.sendNotification(notification, object);
    }

    public void sendNotification(GameNotification gameNotification, Object data, int playerId) {
        try {
            ObjectOutputStream connection = playerId == 0 ? outToHostClientStream : outToConnectingClientStream;
            GameData gameData = new GameData(gameNotification, data);
            connection.writeObject(gameData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
