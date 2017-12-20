package net.demilich.metastone.gui.multiplayermode;

import net.demilich.metastone.GameData;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.gameconfig.MultiplayerConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.gui.playmode.GameContextVisualizable;

import java.io.*;
import java.net.*;

public class Client {

    private final String IP_ADDRESS;
    private final int PORT;
    private final String VERSION;

    private final MultiplayerConfig multiplayerConfig;

    private static ObjectOutputStream outToServerStream;
    private static ObjectInputStream inFromServerStream;

    public boolean inGame = false;

    public Client (MultiplayerConfig config, String version) {
        this.IP_ADDRESS = config.getIpAddress();
        this.PORT = config.getPort();
        this.VERSION = version;
        this.multiplayerConfig = config;
    }

    public static void main(String[] args) {
        MultiplayerConfig multiplayerConfig = new MultiplayerConfig();
        multiplayerConfig.setIpAddress(args[0]);
        multiplayerConfig.setPort(Integer.parseInt(args[1]));
        PlayerConfig playerConfig = new PlayerConfig();
        playerConfig.setName("hey my dudes");
        multiplayerConfig.setPlayerConfig1(playerConfig);
        Client client = new Client(multiplayerConfig, args[2]);
        client.initialize();
    }

    public void initialize() {
        try {
            Socket clientSocket = new Socket(IP_ADDRESS, PORT);


            /*
            PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
            printStream.println(VERSION);
            printStream.close();
            */

            outToServerStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inFromServerStream = new ObjectInputStream(clientSocket.getInputStream());

            outToServerStream.writeObject(multiplayerConfig.getPlayerConfig1());
            synchronized (clientSocket) {
                clientSocket.wait(10000);
            }

            //GameContextVisualizable gameContextVisualizable = (GameContextVisualizable) inFromServerStream.readObject();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    inGame = true;
                    try {
                        while(inGame) {
                            receiveNotification((GameData) inFromServerStream.readObject());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendNotifiation(GameNotification notification, Object data) {
        GameData gameData = new GameData(notification, data);
        try {
            System.out.println("over here?");
            outToServerStream.writeObject(gameData);
            System.out.println("sendy");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveNotification(GameData gameData) {
        GameNotification notification = gameData.getGameNotification();
        Object object = gameData.getData();
        NotificationProxy.sendNotification(notification, object);
    }

    public void endGame() {
        inGame = false;
    }



}
