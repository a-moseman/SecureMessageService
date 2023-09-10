package org.amoseman.securemessageservice.server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientSocketHandler implements Runnable {
    private Socket socket;
    private List<ClientSocketHandler> clientSocketHandlers;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private boolean running;

    public ClientSocketHandler(Socket clientSocket, List<ClientSocketHandler> clientSocketHandlers) {
        this.socket = clientSocket;
        this.clientSocketHandlers = clientSocketHandlers;
    }

    public void run() {
        try {
            inputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            printWriter = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        running = true;
        String line;
        while (running) {
            try {
                line = bufferedReader.readLine();
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    return;
                }
                else {
                    echoAll(line);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        // close
        try {
            bufferedReader.close();
            printWriter.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void echo(String line) {
        printWriter.println(line);
    }

    public void echoAll(String line) {
        System.out.println(new String(socket.getInetAddress().getHostAddress()) + ">" + line);
        clientSocketHandlers.forEach(clientSocketHandler -> clientSocketHandler.echo(line));
    }

    public void quit() {
        running = false;
    }
}
