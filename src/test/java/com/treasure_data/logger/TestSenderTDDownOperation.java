package com.treasure_data.logger;

import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.Unpacker;

import com.treasure_data.logger.Sender;


public class TestSenderTDDownOperation {

    /**
     * if Sender object was created when fluentd doesn't work, ...
     */
    @Test
    public void testFluentdDownOperation01() throws Exception {
        int port = 24224;
        MessagePack msgpack = new MessagePack();
        msgpack.register(Sender.Event.class, Sender.EventTemplate.INSTANCE);
        BufferPacker packer = msgpack.createBufferPacker();
        long timestamp = System.currentTimeMillis();

        // start senders
        Sender sender = new Sender("localhost", port);
        Map<String, String> data = new HashMap<String, String>();
        data.put("t1k1", "t1v1");
        data.put("t1k2", "t1v2");
        sender.emit("tag.label1", timestamp, data);

        packer.write(new Sender.Event("tag.label1", timestamp, data));
        byte[] bytes1 = packer.toByteArray();
        assertArrayEquals(bytes1, sender.getBuffer());

        Map<String, String> data2 = new HashMap<String, String>();
        data2.put("t2k1", "t2v1");
        data2.put("t2k2", "t2v2");
        sender.emit("tag.label2", timestamp, data2);

        packer.write(new Sender.Event("tag.label2", timestamp, data2));
        byte[] bytes2 = packer.toByteArray();
        assertArrayEquals(bytes2, sender.getBuffer());

        // close sender sockets
        sender.close();
    }

    /**
     * if emit method was invoked when fluentd doesn't work, ...
     */
    @Ignore @Test
    public void testFluentdDownOperation02()throws Exception {
        int port = 24224;
        MessagePack msgpack = new MessagePack();
        msgpack.register(Sender.Event.class, Sender.EventTemplate.INSTANCE);
        BufferPacker packer = msgpack.createBufferPacker();
        long timestamp = System.currentTimeMillis();

        // start mock server
        MockServer server = new MockServer(port, new MockServer.MockProcess() {
            public void process(MessagePack msgpack, Socket socket) throws IOException {
                System.out.println("server closing");
                socket.close();
                System.out.println("server closed");
            }
        });
        server.start();

        // start senders
        Sender sender = new Sender("localhost", port);

        // server close
        server.close();

        // sleep a little bit
        Thread.sleep(1000);

        Map<String, String> data = new HashMap<String, String>();
        data.put("t1k1", "t1v1");
        data.put("t1k2", "t1v2");
        for (int i = 0; i < 3; ++i) {
        System.out.println("sender emit");
        sender.emit("tag.label1", data);
        }

        packer.write(new Sender.Event("tag.label1", timestamp, data));
        byte[] bytes1 = packer.toByteArray();
        assertArrayEquals(bytes1, sender.getBuffer());

        Map<String, String> data2 = new HashMap<String, String>();
        data2.put("t2k1", "t2v1");
        data2.put("t2k2", "t2v2");
        sender.emit("tag.label2", data2);

        packer.write(new Sender.Event("tag.label2", timestamp, data2));
        byte[] bytes2 = packer.toByteArray();
        assertArrayEquals(bytes2, sender.getBuffer());

        // close sender sockets
        sender.close();
    }
}
