package org.amoseman.securemessageservice;

import org.amoseman.securemessageservice.core.client.SSLClient;
import org.amoseman.securemessageservice.core.server.SSLServer;

public class Main {
    //https://www.baeldung.com/java-ssl-handshake-failures
    public static void main(String[] args) {
        String executionType = args[0];
        int serverPort = Integer.parseInt(args[1]);

        switch (executionType) {
            case "server":
                SSLServer server = new SSLServer(serverPort);
                server.start();
                break;
            case "client":
                String serverAddress = args[2];
                SSLClient client = new SSLClient(serverPort, serverAddress);
                client.start();
                break;
        }
    }
}