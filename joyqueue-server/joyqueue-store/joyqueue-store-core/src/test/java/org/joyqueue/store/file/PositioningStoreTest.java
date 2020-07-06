/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.store.file;

import org.joyqueue.store.index.IndexItem;
import org.joyqueue.store.index.IndexSerializer;
import org.joyqueue.store.nsm.VirtualThreadExecutor;
import org.joyqueue.store.utils.MessageTestUtils;
import org.joyqueue.store.utils.PreloadBufferPool;
import org.joyqueue.toolkit.time.SystemClock;
import org.joyqueue.toolkit.util.BaseDirUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @author liyue25
 * Date: 2018-11-28
 */
public class PositioningStoreTest {
    private static final Logger logger = LoggerFactory.getLogger(PositioningStoreTest.class);
    private File base = null;
    private File logBase = null;
    private File indexBase = null;

    // write read flush
    // single file/ multiple files
    // flush / not flush
    // cached / file

    @Test
    public void messageWriteReadTest() throws IOException, TimeoutException, InterruptedException {
        VirtualThreadExecutor virtualThreadPool = new VirtualThreadExecutor(500, 100, 10, 1000, 4);
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();

        PositioningStore<ByteBuffer> store =
                new PositioningStore<>(logBase, new PositioningStore.Config(),
                        bufferPool,
                        new StoreMessageSerializer(1024 * 1024));
        store.recover();
        int count = 10;
        String writeMessage = "Hello, world!";
        List<String> bodyList = MessageTestUtils.createBodyList(writeMessage, count);
        long start = store.right();
        List<ByteBuffer> writeMessages = MessageTestUtils.createMessages(bodyList);
        long length = writeMessages.stream().mapToLong(ByteBuffer::remaining).sum();

        long writePosition = store.append(writeMessages);
        Assert.assertEquals(length + start, writePosition);
        Assert.assertEquals(writePosition, store.right());

        List<ByteBuffer> readLogs = store.batchRead(start, writeMessages.size());
        List<String> readBodyList = MessageTestUtils.getBodies(readLogs);
        Assert.assertEquals(bodyList, readBodyList);
        store.close();
        virtualThreadPool.stop();
    }

    // recover
    @Test
    public void messageRecoverTest() throws IOException {
        int count = 10000;
        String writeMessage = "Hello, world!";
        List<String> bodyList = MessageTestUtils.createBodyList(writeMessage, count);
        List<ByteBuffer> writeMessages = MessageTestUtils.createMessages(bodyList);
        long length = writeMessages.stream().mapToLong(ByteBuffer::remaining).sum();
        int fileDataSize = 128 * 1024;
        int cachedPageCount = (int) (length / fileDataSize) + 1;
        PositioningStore.Config config = new PositioningStore.Config(fileDataSize,
                PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE);
        VirtualThreadExecutor virtualThreadPool = new VirtualThreadExecutor(500, 100, 10, 1000, 4);
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();

        StoreMessageSerializer factory = new StoreMessageSerializer(1024 * 1024);
        PositioningStore<ByteBuffer> store =
                new PositioningStore<>(logBase, config,
                        bufferPool,
                        new StoreMessageSerializer(1024));
        store.recover();
        long start = store.right();
        long writePosition = store.append(writeMessages);
        Assert.assertEquals(length + start, writePosition);
        Assert.assertEquals(writePosition, store.right());

        while (store.flush()) {
            Thread.yield();
        }
        store.close();
        store =
                new PositioningStore<>(logBase, config, bufferPool, factory);
        store.recover();

        List<ByteBuffer> readLogs = store.batchRead(start, writeMessages.size());
        List<String> readBodyList = MessageTestUtils.getBodies(readLogs);
        Assert.assertEquals(bodyList, readBodyList);
        store.close();
        virtualThreadPool.stop();

    }


    /**
     * 测试宕机后，是否能正确恢复
     */
    @Test
    public void recoverTailEmptyFilesTest() throws IOException {
        int count = 10000;
        String writeMessage = "Hello, world!";
        List<String> bodyList = MessageTestUtils.createBodyList(writeMessage, count);
        List<ByteBuffer> writeMessages = MessageTestUtils.createMessages(bodyList);
        int fileDataSize = 128 * 1024;
        PositioningStore.Config config = new PositioningStore.Config(fileDataSize,
                PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE);
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();

        StoreMessageSerializer factory = new StoreMessageSerializer(1024 * 1024);
        PositioningStore<ByteBuffer> store =
                new PositioningStore<>(logBase, config,
                        bufferPool,
                        new StoreMessageSerializer(1024));
        store.recover();
        long start = store.right();
        store.append(writeMessages);

        while (store.flush()) {
            Thread.yield();
        }
        store.close();

        File file = new File(logBase, "1440842");
        Assert.assertTrue(file.createNewFile());

        file = new File(logBase, "1571932");
        Assert.assertTrue(file.createNewFile());

        store =
                new PositioningStore<>(logBase, config, bufferPool, factory);
        store.recover();

        List<ByteBuffer> readLogs = store.batchRead(start, writeMessages.size());
        List<String> readBodyList = MessageTestUtils.getBodies(readLogs);
        Assert.assertEquals(bodyList, readBodyList);
        store.close();

    }




    // setRight

    @Test
    public void messageSetRightTest() throws IOException, InterruptedException, TimeoutException {
        VirtualThreadExecutor virtualThreadPool = new VirtualThreadExecutor(500, 100, 10, 1000, 4);
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();
        PositioningStore<ByteBuffer> store =
                new PositioningStore<>(logBase, new PositioningStore.Config(),
                        bufferPool,
                        new StoreMessageSerializer(1024 * 1024));
        store.recover();
        int count = 10;
        String writeMessage = "Hello, world!";
        List<String> bodyList = MessageTestUtils.createBodyList(writeMessage, count);
        long start = store.right();
        List<ByteBuffer> writeMessages = MessageTestUtils.createMessages(bodyList);
        long length = writeMessages.stream().mapToLong(ByteBuffer::remaining).sum();

        long writePosition = store.append(writeMessages);
        Assert.assertEquals(length + start, writePosition);
        Assert.assertEquals(writePosition, store.right());


        long position = IntStream.range(0, 7).mapToObj(writeMessages::get).mapToInt(ByteBuffer::capacity).sum();
        store.setRight(position);
        Assert.assertEquals(position, store.right());

        bodyList.remove(9);
        bodyList.remove(8);
        bodyList.remove(7);

        List<ByteBuffer> readLogs = store.batchRead(start, bodyList.size());
        List<String> readBodyList = MessageTestUtils.getBodies(readLogs);
        Assert.assertEquals(bodyList, readBodyList);

        while (store.flush()) {
            Thread.yield();
        }

        bodyList.remove(6);
        position = IntStream.range(0, 6).mapToObj(writeMessages::get).mapToInt(ByteBuffer::capacity).sum();

        store.setRight(position);


        readLogs = store.batchRead(start, bodyList.size());
        readBodyList = MessageTestUtils.getBodies(readLogs);
        Assert.assertEquals(bodyList, readBodyList);

        position += 1;
        store.setRight(position);
        Assert.assertEquals(position, store.right());
        store.close();
        virtualThreadPool.stop();

    }

    @Test
    public void indexWriteReadTest() throws IOException, InterruptedException, TimeoutException {
        VirtualThreadExecutor virtualThreadPool = new VirtualThreadExecutor(500, 100, 10, 1000, 4);
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();
        PositioningStore<IndexItem> store =
                new PositioningStore<>(logBase, new PositioningStore.Config(),
                        bufferPool,
                        new IndexSerializer());
        store.recover();
        int count = 10;
        short partition = 3;

        List<IndexItem> indexItems = IntStream.range(0, count)
                .mapToObj(i -> new IndexItem(partition, i, 666, 888))
                .collect(Collectors.toList());

        long start = store.right();
        long length = IndexItem.STORAGE_SIZE * count;

        long writePosition = store.append(indexItems);
        Assert.assertEquals(length + start, writePosition);
        Assert.assertEquals(writePosition, store.right());

        List<IndexItem> readLogs = store.batchRead(start, indexItems.size());
        IntStream.range(0, count).forEach(i -> {
            Assert.assertEquals(indexItems.get(i).getOffset(), readLogs.get(i).getOffset());
            Assert.assertEquals(indexItems.get(i).getLength(), readLogs.get(i).getLength());
        });
        store.close();
        virtualThreadPool.stop();

    }

    // recover
    @Test
    public void indexRecoverTest() throws IOException, InterruptedException, TimeoutException {
        PositioningStore.Config config = new PositioningStore.Config(128);
        VirtualThreadExecutor virtualThreadPool = new VirtualThreadExecutor(500, 100, 10, 1000, 4);
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();
        PositioningStore<IndexItem> store =
                new PositioningStore<>(logBase, new PositioningStore.Config(),
                        bufferPool,
                        new IndexSerializer());
        store.recover();
        int count = 10;
        short partition = 3;

        List<IndexItem> indexItems = IntStream.range(0, count)
                .mapToObj(i -> new IndexItem(partition, i, 666, 888))
                .collect(Collectors.toList());

        long start = store.right();
        long length = IndexItem.STORAGE_SIZE * count;

        long writePosition = store.append(indexItems);
        Assert.assertEquals(length + start, writePosition);
        Assert.assertEquals(writePosition, store.right());

        while (store.flush()) {
            Thread.yield();
        }
        store.close();
        store =
                new PositioningStore<>(logBase, config, bufferPool, new IndexSerializer());
        store.recover();
        List<IndexItem> readLogs = store.batchRead(start, indexItems.size());
        IntStream.range(0, count).forEach(i -> {
            Assert.assertEquals(indexItems.get(i).getOffset(), readLogs.get(i).getOffset());
            Assert.assertEquals(indexItems.get(i).getLength(), readLogs.get(i).getLength());
        });

        store.close();
        virtualThreadPool.stop();
    }


    // -XX:MaxDirectMemorySize=12g
    @Test
    public void writePerformanceTest() throws IOException, InterruptedException, TimeoutException {
        // 总共写入消息的的大小
        long maxSize = 1L * 1024 * 1024 * 1024;
        // 每条消息消息体大小
        int logSize = 12;
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();
        try (PositioningStore<ByteBuffer> store = prepareStore(bufferPool)) {

            ByteBuffer buffer = MessageTestUtils.createMessage(new byte[logSize]);
            write(store, maxSize, buffer);
        }
    }

    @Test
    public void readPerformanceTest() throws IOException, InterruptedException, TimeoutException {
        // 总共写入消息的的大小
        long maxSize = 1L * 1024 * 1024 * 1024;
        // 每条消息消息体大小
        int logSize = 1024;
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();
        try (PositioningStore<ByteBuffer> store = prepareStore(bufferPool)) {
            ByteBuffer buffer = MessageTestUtils.createMessage(new byte[logSize]);
            final int msgSize = buffer.remaining();
            long writeSize = write(store, maxSize, buffer);
            read(store, msgSize, writeSize);
        }
    }

    @Test
    public void readByteBufferPerformanceTest() throws IOException, InterruptedException, TimeoutException {
        // 总共写入消息的的大小
        long maxSize = 1L * 1024 * 1024 * 1024;
        // 每条消息消息体大小
        int logSize = 1024;
        // 每批读消息大小
        int batchSize = 10 * 1024;

        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();
        try (PositioningStore<ByteBuffer> store = prepareStore(bufferPool)) {
            ByteBuffer buffer = MessageTestUtils.createMessage(new byte[logSize]);
            long writeSize = write(store, maxSize, buffer);
            readByteBuffer(store, batchSize, writeSize);
        }
    }

    private PositioningStore<ByteBuffer> prepareStore(PreloadBufferPool bufferPool) throws IOException {
        return prepareStore(bufferPool, new PositioningStore.Config());
    }

    private PositioningStore<ByteBuffer> prepareStore(PreloadBufferPool bufferPool, PositioningStore.Config config) throws IOException {
        StoreMessageSerializer storeMessageSerializer = new StoreMessageSerializer(2 * 1024);
        bufferPool.addPreLoad(PositioningStore.Config.DEFAULT_FILE_DATA_SIZE, 2, 5);
        PositioningStore<ByteBuffer> store =
                new PositioningStore<>(logBase, config,
                        bufferPool,
                        storeMessageSerializer);
        store.recover();
        return store;
    }


    private void readByteBuffer(PositioningStore<ByteBuffer> store, int batchSize, long maxSize) throws IOException {
        long start;
        long position;
        long t;
        long spendTimeMs;
        long mbps;

        start = SystemClock.now();
        position = 0;
        while (position < maxSize) {

            ByteBuffer byteBuffer = store.readByteBuffer(position, batchSize);
            position += byteBuffer.remaining();
        }
        t = SystemClock.now();
        spendTimeMs = t - start;
        mbps = maxSize * 1000 / spendTimeMs / 1024 / 1024;

        logger.info("Replication read performance: {} MBps.", mbps);
        Assert.assertEquals(maxSize, position);
    }

    private void read(PositioningStore<ByteBuffer> store, int msgSize, long maxSize) throws IOException {
        long start;
        long t;
        long spendTimeMs;
        long mbps;

        long position = 0;

        start = SystemClock.now();
        while (position < maxSize) {
            position += store.read(position, msgSize).remaining();
        }

        t = SystemClock.now();
        spendTimeMs = t - start;
        mbps = maxSize * 1000 / spendTimeMs / 1024 / 1024;

        logger.info("Read performance: {} MBps.", mbps);
        Assert.assertEquals(maxSize, position);
    }

    private long write(PositioningStore<ByteBuffer> store, long maxSize, ByteBuffer message) throws InterruptedException, IOException {
        long size = 0;
        VirtualThreadExecutor virtualThreadPool = new VirtualThreadExecutor(5000, 200, 10, 1000, 2);
        virtualThreadPool.start(store::flush, 100, "flush");
        Thread.sleep(1000);
        long start = SystemClock.now();
        while (size < maxSize) {
            size = store.append(message.slice());
        }
        long t = SystemClock.now();
        long spendTimeMs = t - start;
        long mbps = spendTimeMs > 0 ? size * 1000 / spendTimeMs / 1024 / 1024 : 0;

        logger.info("Final write size: {}, write performance: {} MBps.", size, mbps);

        while (store.flushPosition() < store.right()) {
            Thread.sleep(10);
        }
        t = SystemClock.now();
        spendTimeMs = t - start;
        mbps = spendTimeMs > 0 ? size * 1000 / spendTimeMs / 1024 / 1024 : 0;

        logger.info("Flush performance: {} MBps.", mbps);
        virtualThreadPool.stop();
        return size;
    }


    // position
    @Test
    public void positionTest() throws Exception {
        PositioningStore.Config config = new PositioningStore.Config();
        VirtualThreadExecutor virtualThreadPool = new VirtualThreadExecutor(500, 100, 10, 1000, 4);
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();
        PositioningStore<ByteBuffer> store =
                new PositioningStore<>(logBase, new PositioningStore.Config(),
                        bufferPool,
                        new StoreMessageSerializer(1024 * 1024));
        store.recover();
        int count = 10;
        String writeMessage = "Hello, world!";
        List<String> bodyList = MessageTestUtils.createBodyList(writeMessage, count);
        List<ByteBuffer> writeMessages = MessageTestUtils.createMessages(bodyList);

        long writePosition = store.append(writeMessages);

        long position = store.position(writePosition, 3);
        Assert.assertEquals(writePosition, position);

        position = store.position(position, -1);
        long expectPosition = IntStream.range(0, 9).mapToLong(i -> writeMessages.get(i).capacity()).sum();
        Assert.assertEquals(expectPosition, position);

        position = position + 8;

        position = store.position(position, -5);
        expectPosition = IntStream.range(0, 4).mapToLong(i -> writeMessages.get(i).capacity()).sum();
        Assert.assertEquals(expectPosition, position);

        position = position - 7;
        position = store.position(position, -3);
        expectPosition = 0;
        Assert.assertEquals(expectPosition, position);

        position = store.position(position, 5);
        expectPosition = IntStream.range(0, 5).mapToLong(i -> writeMessages.get(i).capacity()).sum();
        Assert.assertEquals(expectPosition, position);

        position += 7;

        position = store.position(position, 2);
        expectPosition = IntStream.range(0, 7).mapToLong(i -> writeMessages.get(i).capacity()).sum();
        Assert.assertEquals(expectPosition, position);
        position -= 7;
        position = store.position(position, 0);
        expectPosition = IntStream.range(0, 6).mapToLong(i -> writeMessages.get(i).capacity()).sum();
        Assert.assertEquals(expectPosition, position);

        position = store.position(position, 10);
        expectPosition = IntStream.range(0, 10).mapToLong(i -> writeMessages.get(i).capacity()).sum();
        Assert.assertEquals(expectPosition, position);


    }

    // delete

    @Test
    public void deleteTest() throws Exception {
        // 每条消息消息体大小
        int logSize = 1024;

        ByteBuffer buffer = MessageTestUtils.createMessage(new byte[logSize]);

        // 总共写入消息的的大小
        long maxSize = 10 * 1024 * 1024;

        int fileSize = 1024 * buffer.capacity();

        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();
        try (PositioningStore<ByteBuffer> store = prepareStore(bufferPool, new PositioningStore.Config(fileSize, 128, 90))) {
            write(store, maxSize, buffer);
            long currentRight = store.right();
            // 删除一个文件
            Assert.assertEquals(fileSize, store.physicalDeleteLeftFile());
            Assert.assertEquals(fileSize, store.left());

            // 给定位置删除半个文件，由于不够一个文件的长度，什么都没有删除
            Assert.assertEquals(0L, store.physicalDeleteTo(fileSize + buffer.capacity() * 512));
            Assert.assertEquals(fileSize, store.left());

            // 给定位置删除1.5个文件，实际删除一个文件。
            Assert.assertEquals(fileSize, store.physicalDeleteTo( fileSize + fileSize + buffer.capacity() * 512));
            Assert.assertEquals(fileSize + fileSize, store.left());

            while (store.physicalDeleteLeftFile() > 0) {
               // nothing to do
            }



            Assert.assertEquals(currentRight, store.left());
            Assert.assertEquals(currentRight, store.flushPosition());
            Assert.assertEquals(currentRight, store.right());

            store.setRight(0L);

            AtomicBoolean stop = new AtomicBoolean(false);

            Thread t = new Thread(() -> {
                while (!stop.get()) {
                    try {
                        store.physicalDeleteLeftFile();
                    } catch (Exception e) {
                        logger.warn("Exception: ", e);
                    }
                }
            });
            t.start();
            write(store, maxSize, buffer);
            stop.set(true);

        }

    }


    @Before
    public void before() throws Exception {
        prepareBaseDir();

    }

    @After
    public void after() {
        destroyBaseDir();

    }

    private void destroyBaseDir() {
        BaseDirUtils.destroyBaseDir(base);
        base = null;
        logBase = null;
        indexBase = null;
    }

    private void prepareBaseDir() throws IOException {

        base = BaseDirUtils.prepareBaseDir();
        logger.info("Base directory: {}.", base.getCanonicalPath());
        logBase = new File(base, "log");
        indexBase = new File(base, "index");
        logBase.mkdir();
        indexBase.mkdir();

    }

}