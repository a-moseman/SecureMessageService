package org.amoseman.securemessageservice;

import org.amoseman.securemessageservice.client.Client;
import org.amoseman.securemessageservice.server.Server;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String executionType = args[0];
        int serverPort = Integer.parseInt(args[1]);


        switch (executionType) {
            case "server":
                Server server = new Server(serverPort);
                server.run();
                break;
            case "client":
                String serverAddress = args[2];
                Client client = new Client(serverAddress, serverPort);
                Thread thread = new Thread(client);
                thread.start();

                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String line = scanner.nextLine();
                    if (line.equalsIgnoreCase("QUIT")) {
                        break;
                    }
                    client.sendMessage(line);
                }
                break;
        }
    }
}