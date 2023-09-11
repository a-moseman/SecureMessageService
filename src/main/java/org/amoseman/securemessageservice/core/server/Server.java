package org.amoseman.securemessageservice.core.server;

import org.amoseman.securemessageservice.core.Application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server
 */
public class Server extends Application {
    private int port;
    private boolean running;
    private List<ClientSocketHandler> clientSocketHandlers;
    private ServerSocket serverSocket;

    public Server(int port) {
        super();
        this.port = port;
    }

    public void run() {
        System.out.println("Starting server on port " + port + "...");
        clientSocketHandlers = Collections.synchronizedList(new ArrayList<>());
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        running = true;
        while (running) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            onClientConnect(clientSocket);
        }
    }

    private void onClientConnect(Socket clientSocket) {
        ClientSocketHandler clientSocketHandler = new ClientSocketHandler(clientSocket, clientSocketHandlers, PGP_KEY_PAIR, CRYPTOGRAPHY);
        clientSocketHandlers.add(clientSocketHandler);
        Thread thread = new Thread(clientSocketHandler);
        thread.start();
        System.out.println("Client at " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " has connected.");
    }

    public void quit() {
        running = false;
        clientSocketHandlers.forEach((ClientSocketHandler::quit));
    }
}
