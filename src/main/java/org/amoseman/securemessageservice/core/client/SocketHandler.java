package org.amoseman.securemessageservice.core.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketHandler implements Runnable {
    private final String SERVER_ADDRESS;
    private final int SERVER_PORT;
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private boolean running;

    public SocketHandler(String serverAddress, int serverPort) {
        this.SERVER_ADDRESS = serverAddress;
        this.SERVER_PORT = serverPort;
    }

    public void run() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        running = true;
        while (running) {
            try {
                String line = bufferedReader.readLine();
                System.out.println(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        printWriter.println(message);
    }

    public void quit() {
        running = false;
        try {
            bufferedReader.close();
            printWriter.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
