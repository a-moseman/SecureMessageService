package org.amoseman.securemessageservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server extends Runnable {
    public static final int PORT = 1000; // TODO: change
    private boolean running;
    private List<Socket> clientSockets;
    private ServerSocket serverSocket;

    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            new ClientSocketThread(clientSocket);
        }
    }

    public ServerSocket() throws IOException {
        serverSocket = new ServerSocket(PORT);
        while (true) {
            socket
        }
    }
}
