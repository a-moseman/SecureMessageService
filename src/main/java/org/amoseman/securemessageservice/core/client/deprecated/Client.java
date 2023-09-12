package org.amoseman.securemessageservice.core.client.deprecated;

import org.amoseman.securemessageservice.core.Application;

import java.util.Scanner;

public class Client extends Application {
    private final String SERVER_ADDRESS;
    private final int SERVER_PORT;

    public Client(String serverAddress, int serverPort) {
        super();
        this.SERVER_ADDRESS = serverAddress;
        this.SERVER_PORT = serverPort;
    }

    public void run() {
        SocketHandler socketHandler =  new SocketHandler(SERVER_ADDRESS, SERVER_PORT, PGP_KEY_PAIR, CRYPTOGRAPHY);
        Thread thread = new Thread(socketHandler);
        thread.start();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("QUIT")) {
                break;
            }
            socketHandler.sendMessage(line, true);
        }
    }
}
