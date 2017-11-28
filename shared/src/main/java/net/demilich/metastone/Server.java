package net.demilich.metastone;

import java.net.*;
import java.io.*;

public class Server {

    public static final int PORT = 25565;

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.run();
    }

    public void run() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);


        Socket clientSocket = serverSocket.accept();


        ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());



    }

    public void startGame() {

    }

}
