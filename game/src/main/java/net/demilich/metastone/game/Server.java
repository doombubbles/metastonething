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

    public static boolean initialized;
    private static boolean exceptions;

    public static void main(String[] args){
        MultiplayerConfig config = new MultiplayerConfig();
        config.setPort(Integer.parseInt(args[0]));
        initialize(config, args[1], true);
    }

    public static void initialize(MultiplayerConfig multiplayerConfig, String version, boolean Exceptions){
        if (initialized) {
            return;
        }
        initialized = true;
        exceptions = Exceptions;
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
            if (exceptions) {
                e.printStackTrace();
            }
        }
    }

    /*
public static String handleVersion(Socket socket) {
    try {
        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String version = bufferedReader.readLine();
        System.out.println("We got to the version part of it! " + version);
        return version;

    } catch (IOException e) {
        if (exceptions) {
            return null;
        }

    }
}
*/
    public static Object readObject(int playerId){
        if (playerId == 1) {
            try {
                return inFromConnectingClientStream.readObject();
            } catch (IOException e) {
            } catch (ClassNotFoundException e) {
            }
        } else try {
            return inFromHostClientStream.readObject();
        }  catch (ClassNotFoundException e) {
            if (exceptions) {
                e.printStackTrace();

            }
        } catch (SocketException e) {
        }catch (IOException e) {
            if (exceptions) {
                e.printStackTrace();

            }
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
            if (exceptions) {
                e.printStackTrace();
            }
        }
    }

    public static ObjectInputStream getInFromConnectingClientStream() {
        return inFromConnectingClientStream;
    }

    public static ObjectInputStream getInFromHostClientStream() {
        return inFromHostClientStream;
    }

    public static void rip() {
        initialized = false;
        if (outToConnectingClientStream != null) {
            try {
                sendNotification(GameNotification.GAME_OVER, null, 1);
                outToConnectingClientStream.close();
                System.out.println("Succesfully closed server stream # 1");
            } catch (IOException e) {
            }
        }
        if (outToHostClientStream != null) {
            try {
                sendNotification(GameNotification.GAME_OVER, null, 0);
                outToHostClientStream.close();
                System.out.println("Succesfully closed server stream # 2");
            } catch (IOException e) {
            }
        }
        if (inFromConnectingClientStream != null) {
            try {
                inFromConnectingClientStream.close();
                System.out.println("Succesfully closed server stream # 3");
            } catch (IOException e) {
            }
        }
        if (inFromHostClientStream != null) {
            try {
                inFromHostClientStream.close();
            } catch (IOException e) {
            }
        }
        if (serverSocket != null) {
            System.out.println("This server is shuttin down");
            System.out.println("Succesfully closed server stream # 4");
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }

    }


}
