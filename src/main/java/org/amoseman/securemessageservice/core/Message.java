package org.amoseman.securemessageservice.core;

import java.net.InetAddress;

public class Message {
    public final InetAddress SOURCE;
    public final String MESSAGE;

    public Message(InetAddress source, String message) {
        SOURCE = source;
        MESSAGE = message;
    }
}
