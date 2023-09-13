package org.amoseman.securemessageservice.core.server;

import org.amoseman.securemessageservice.core.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connection implements Runnable {
    private final Socket SOCKET;
    private final ConcurrentLinkedQueue<Message> MESSAGE_QUEUE;
    public final PublicKey CLIENT_PUBLIC_KEY;
    private final DataOutputStream OUTPUT_STREAM;
    private final DataInputStream INPUT_STREAM;
    private boolean running;

    public Connection(Socket socket, ConcurrentLinkedQueue<Message> messageQueue, PublicKey clientPublicKey) {
        SOCKET = socket;
        MESSAGE_QUEUE = messageQueue;
        CLIENT_PUBLIC_KEY = clientPublicKey;
        try {
            OUTPUT_STREAM = new DataOutputStream(SOCKET.getOutputStream());
            INPUT_STREAM = new DataInputStream(SOCKET.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            receiveMessage();
        }
    }

    private void receiveMessage() {
        try {
            String utf = INPUT_STREAM.readUTF();
            Message message = new Message(getAddress(), utf);
            MESSAGE_QUEUE.add(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        try {
            OUTPUT_STREAM.writeBytes(message);
            OUTPUT_STREAM.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InetAddress getAddress() {
        return SOCKET.getInetAddress();
    }
}
