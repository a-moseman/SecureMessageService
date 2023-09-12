package org.amoseman.securemessageservice.core.client;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class SSLClient {
    private final SSLSocket SOCKET;
    private final ObjectOutputStream OUTPUT_STREAM;
    private final ObjectInputStream INPUT_STREAM;
    private boolean running;

    public SSLClient(int port, String address) {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            this.SOCKET = (SSLSocket) sslSocketFactory.createSocket(address, port);
            SOCKET.setNeedClientAuth(true);
            SOCKET.setEnabledCipherSuites(new String[]{"TLS_DHE_DSS_WITH_AES_256_CBC_SHA256"});
            SOCKET.setEnabledProtocols(new String[]{"TLSv1.2"});
            SSLParameters parameters = new SSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            SOCKET.setSSLParameters(parameters);

            this.OUTPUT_STREAM = new ObjectOutputStream(SOCKET.getOutputStream());
            this.INPUT_STREAM = new ObjectInputStream(SOCKET.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        running = true;
        new Thread(() -> {
            while (running) {
                try {
                    String message = INPUT_STREAM.readUTF();
                    System.out.println(message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        Scanner scanner = new Scanner(System.in);
        while (running) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("QUIT")) {
                dispose();
            }
            else {
                send(input);
            }
        }
    }

    private void send(String message) {
        try {
            OUTPUT_STREAM.writeObject(message);
            OUTPUT_STREAM.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispose() {
        try {
            OUTPUT_STREAM.close();
            INPUT_STREAM.close();
            SOCKET.close();
            running = false;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
