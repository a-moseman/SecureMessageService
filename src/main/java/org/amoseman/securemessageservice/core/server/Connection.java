package org.amoseman.securemessageservice.core.server;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connection implements Runnable {
    private final SSLSocket SOCKET;
    private final ObjectOutputStream OUTPUT_STREAM;
    private final ObjectInputStream INPUT_STREAM;
    private final ConcurrentLinkedQueue<String> MESSAGE_QUEUE;
    private boolean running;

    public Connection(SSLSocket socket, ConcurrentLinkedQueue<String> messageQueue) {
        this.SOCKET = socket;
        try {
            this.OUTPUT_STREAM = new ObjectOutputStream(SOCKET.getOutputStream());
            OUTPUT_STREAM.flush();
            this.INPUT_STREAM = new ObjectInputStream(SOCKET.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.MESSAGE_QUEUE = messageQueue;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            waitForMessage();
        }
    }

    private void waitForMessage() {
        try {
            String message = INPUT_STREAM.readUTF();
            MESSAGE_QUEUE.add(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String message) {
        try {
            OUTPUT_STREAM.writeObject(message);
            OUTPUT_STREAM.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
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
