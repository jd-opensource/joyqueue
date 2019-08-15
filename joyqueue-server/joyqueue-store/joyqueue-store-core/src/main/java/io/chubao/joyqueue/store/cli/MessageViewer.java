package io.chubao.joyqueue.store.cli;

import io.chubao.joyqueue.store.file.PositioningStore;
import io.chubao.joyqueue.store.file.StoreMessageSerializer;
import io.chubao.joyqueue.store.message.MessageParser;
import io.chubao.joyqueue.store.utils.PreloadBufferPool;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author liyue25
 * Date: 2018/10/26
 */
public class MessageViewer {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            showUsage();
            return;
        }

        File base = new File(args[0]);
        if (!base.isDirectory()) {
            System.out.println("Invalid path!");
            return;
        }
        long position = 0L;
        int count = 0;

        if (args.length > 1) {
            position = Long.parseLong(args[1]);

            if (args.length > 2) {
                count = Integer.parseInt(args[2]);
            }
        }

        System.out.println(String.format("Path: %s, position: %d, count: %d", base.getAbsolutePath(), position, count));

        PositioningStore.Config storeConfig =
                new PositioningStore.Config();


        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();

        PositioningStore<ByteBuffer> store =
                new PositioningStore<>(base, storeConfig, bufferPool, new StoreMessageSerializer(1024 * 1024));
        store.recover();
        long pos = store.position(position, -1 * count);
        int wCount = 2 * count + 1;
        for (int i = 0; i < wCount; i++) {

            ByteBuffer buffer = store.read(pos);
            System.out.println(String.format("Message: %d", pos));
            pos += buffer.remaining();
            System.out.println(MessageParser.getString(buffer) + "\n");
        }
    }


    private static void showUsage() {
        System.out.println("Usage: MessageViewer path [position] [count]");
    }
}
