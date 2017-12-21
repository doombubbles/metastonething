package net.demilich.metastone.game;

import net.demilich.metastone.GameData;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.gameconfig.MultiplayerConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;

import java.net.*;
import java.io.*;

public class Server {

    private static int PORT;

    private static ObjectOutputStream outToHostClientStream;
    private static ObjectInputStream inFromHostClientStream;

    private static ObjectOutputStream outToConnectingClientStream;
    private static ObjectInputStream inFromConnectingClientStream;

    public static Socket hostSocket;
    public static Socket connectingSocket;
    public static ServerSocket serverSocket;

    public static void main(String[] args){
        MultiplayerConfig config = new MultiplayerConfig();
        config.setPort(Integer.parseInt(args[0]));
        initialize(config, args[1]);
    }

    public static void initialize(MultiplayerConfig multiplayerConfig, String version){
        PORT = multiplayerConfig.getPort();
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Yay! We have a server socket!");
            hostSocket = serverSocket.accept();
            /*
            if (!VERSION.equals(handleVersion(hostSocket))) {
                throw new IOException("RIP");
            }
            */
            outToHostClientStream = new ObjectOutputStream(hostSocket.getOutputStream());
            inFromHostClientStream = new ObjectInputStream(hostSocket.getInputStream());

            multiplayerConfig.setPlayerConfig1((PlayerConfig) inFromHostClientStream.readObject());


            connectingSocket = serverSocket.accept();
            /*
            if (!VERSION.equals(handleVersion(connectingSocket))) {
                throw new IOException("RIP2");
            }
            */
            outToConnectingClientStream = new ObjectOutputStream(connectingSocket.getOutputStream());
            inFromConnectingClientStream = new ObjectInputStream(connectingSocket.getInputStream());
            multiplayerConfig.setPlayerConfig2((PlayerConfig) inFromConnectingClientStream.readObject());


            NotificationProxy.sendNotification(GameNotification.START_MULTIPLAYER, multiplayerConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static Object readObject(int playerId){
        if (playerId == 1) {
            try {
                return inFromConnectingClientStream.readObject();
            } catch (IOException e) {
            } catch (ClassNotFoundException e) {
            }
        } else try {
            return inFromHostClientStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendNotification(GameNotification gameNotification, Object data, int playerId) {
        try {
            ObjectOutputStream connection = playerId == 0 ? outToHostClientStream : outToConnectingClientStream;
            GameData gameData = new GameData(gameNotification, data);
            connection.writeObject(gameData);
            connection.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ObjectInputStream getInFromConnectingClientStream() {
        return inFromConnectingClientStream;
    }

    public static ObjectInputStream getInFromHostClientStream() {
        return inFromHostClientStream;
    }

    public static void rip() throws IOException {
        if (serverSocket != null) {
            sendNotification(GameNotification.GAME_OVER, null, 0);
            sendNotification(GameNotification.GAME_OVER, null, 1);
            serverSocket.close();
        }
        if (inFromConnectingClientStream != null) {
            inFromConnectingClientStream.close();
        }
        if (inFromHostClientStream != null) {
            inFromHostClientStream.close();
        }
        if (outToConnectingClientStream != null) {
            outToConnectingClientStream.close();
        }
        if (outToHostClientStream != null) {
            outToHostClientStream.close();
        }
    }


}
