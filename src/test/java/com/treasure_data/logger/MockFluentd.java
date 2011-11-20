package com.treasure_data.logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.fluentd.logger.sender.Event;
import org.fluentd.logger.sender.EventTemplate;
import org.fluentd.logger.sender.Sender;
import org.msgpack.MessagePack;

public class MockFluentd extends Thread {

    public static interface MockProcess {
        public void process(MessagePack msgpack, Socket socket) throws IOException;
    }

    private MessagePack msgpack;

    private ServerSocket serverSocket;

    private MockProcess process;

    public MockFluentd(int port, MockProcess mockProcess) throws IOException {
        msgpack = new MessagePack();
        msgpack.register(Event.class, EventTemplate.INSTANCE);
        serverSocket = new ServerSocket(port);
        process = mockProcess;
    }

    public void run() {
        try {
            final Socket socket = serverSocket.accept();
            Thread th = new Thread() {
                public void run() {
                    try {
                        process.process(msgpack, socket);
                    } catch (IOException e) { // ignore
                    }
                }
            };
            th.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
