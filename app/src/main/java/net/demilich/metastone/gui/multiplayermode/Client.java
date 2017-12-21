package net.demilich.metastone.gui.multiplayermode;

import net.demilich.metastone.GameData;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.gameconfig.MultiplayerConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.gui.playmode.GameContextVisualizable;

import java.io.*;
import java.net.*;

public class Client {

    private static String IP_ADDRESS;
    private static int PORT;
    private static String VERSION;

    private static MultiplayerConfig multiplayerConfig;

    private static ObjectOutputStream outToServerStream;
    private static ObjectInputStream inFromServerStream;

    private static Thread t;

    public static Socket clientSocket;

    public static boolean inGame = false;

    public static boolean blockedByAnimation = false;

    /*
    public Client (MultiplayerConfig config, String version) {
        this.IP_ADDRESS = config.getIpAddress();
        this.PORT = config.getPort();
        this.VERSION = version;
        this.multiplayerConfig = config;
    }
    */
    public static void main(String[] args) {
        MultiplayerConfig multiplayerConfig = new MultiplayerConfig();
        multiplayerConfig.setIpAddress(args[0]);
        multiplayerConfig.setPort(Integer.parseInt(args[1]));
        PlayerConfig playerConfig = new PlayerConfig();
        playerConfig.setName("hey my dudes");
        multiplayerConfig.setPlayerConfig1(playerConfig);
        initialize(multiplayerConfig, args[2]);
    }

    public static void initialize(MultiplayerConfig config, String version) {
        IP_ADDRESS = config.getIpAddress();
        PORT = config.getPort();
        VERSION = version;
        multiplayerConfig = config;
        try {
            clientSocket = new Socket(IP_ADDRESS, PORT);

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

            t = new Thread(new Runnable() {
                @Override
                public void run() {
                    inGame = true;
                    try {
                        while(inGame) {
                            receiveNotification((GameData) inFromServerStream.readObject());
                        }
                    } catch (IOException e) {
                    } catch (ClassNotFoundException e) {
                    }
                }
            });
            t.setDaemon(true);
            t.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void sendNotification(GameNotification notification, Object data) {
        GameData gameData = new GameData(notification, data);
        try {
            outToServerStream.writeObject(gameData);
            outToServerStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveNotification(GameData gameData) {
        GameNotification notification = gameData.getGameNotification();
        Object object = gameData.getData();
        NotificationProxy.sendNotification(notification, object);
    }

    public static void endGame() {
        inGame = false;
    }

    public static ObjectOutputStream getOutToServerStream() {
        return outToServerStream;
    }

    public static void rip() throws IOException {
        endGame();
        t.interrupt();
        if (clientSocket != null) {
            clientSocket.close();
        }
        if (outToServerStream != null) {
            outToServerStream.close();
        }
        if (inFromServerStream != null) {
            outToServerStream.close();
        }
    }
}
