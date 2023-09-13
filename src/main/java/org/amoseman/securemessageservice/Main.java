package org.amoseman.securemessageservice;

import org.amoseman.securemessageservice.core.client.Client;
import org.amoseman.securemessageservice.core.server.Server;

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
                Client client = new Client(serverPort, serverAddress, System.in);
                client.run();
                break;
        }
    }
}