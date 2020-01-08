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
package org.joyqueue.store;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.store.file.PositioningStore;
import org.joyqueue.store.message.MessageParser;
import org.joyqueue.store.utils.MessageUtils;
import org.joyqueue.store.utils.PreloadBufferPool;
import org.joyqueue.toolkit.concurrent.EventFuture;
import org.joyqueue.toolkit.concurrent.LoopThread;
import org.joyqueue.toolkit.format.Format;
import org.joyqueue.toolkit.time.SystemClock;
import org.joyqueue.toolkit.util.BaseDirUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_FLUSH_INTERVAL_MS;
import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_MAX_DIRTY_SIZE;
import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_MAX_MESSAGE_LENGTH;
import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_WRITE_REQUEST_CACHE_SIZE;
import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_WRITE_TIMEOUT_MS;

/**
 * @author liyue25
 * Date: 2018/8/30
 */
public class PartitionGroupStoreManagerTest {
    private static final Logger logger = LoggerFactory.getLogger(PartitionGroupStoreManagerTest.class);
    private static final String topic = "test_topic";
    private static final int partitionGroup = 3;
    private static final short[] partitions = new short[]{4, 5, 6};
    private static final int[] nodes = new int[]{0};
    private File base = null;
    private File groupBase = null;
    private PartitionGroupStoreManager store;
    private PreloadBufferPool bufferPool;

    @Test
    public void writeReadTest() throws Exception {

        writeReadTest(QosLevel.RECEIVE);
        after();
        before();
        writeReadTest(QosLevel.PERSISTENCE);
        after();
        before();
        writeReadTest(QosLevel.REPLICATION);
        after();
        before();
        writeReadTest(QosLevel.ALL);

    }


    @Test
    public void indexLengthTest() throws Exception {
        int count = 1024 * 1024;
        long timeout = 500000L;
        long length = 0L;

        for (int i = 0; i < count; i++) {
            int bodySize = ThreadLocalRandom.current().nextInt(127) + 1;
            ByteBuffer msg = MessageUtils.build(1, bodySize).get(0);
            length += msg.remaining();
            store.asyncWrite(QosLevel.ONE_WAY, null, new WriteRequest(partitions[ThreadLocalRandom.current().nextInt(partitions.length)], msg));
        }
        // 等待建索引都完成
        long t0 = SystemClock.now();
        while (SystemClock.now() - t0 < timeout && store.rightPosition() < length) {
            Thread.sleep(10L);
        }
        store.commit(store.rightPosition());
        for (int i = 0; i < partitions.length; i++) {

            for (long j = 0; j < store.getRightIndex(partitions[i]); j++) {
                ReadResult readResult = store.read(partitions[i], j, 1, 0);
                Assert.assertEquals(JoyQueueCode.SUCCESS, readResult.getCode());
                Assert.assertEquals(1, readResult.getMessages().length);
                ByteBuffer readBuffer = readResult.getMessages()[0];
                Assert.assertEquals(readBuffer.getInt(0), readBuffer.remaining());
                length -= readBuffer.remaining();
            }
        }

        Assert.assertEquals(0L, length);

    }

    @Ignore
    @Test
    public void writePerformanceTest() throws InterruptedException {
        writePerformanceTest(5L * 1024 * 1024 * 1024, 1024, 10, false, QosLevel.REPLICATION);
    }

    private void writePerformanceTest(long totalBytes, int msgSize, int batchCount, boolean sync, QosLevel qosLevel) throws InterruptedException {
        List<ByteBuffer> messages = MessageUtils.build(batchCount, msgSize);
        int size = messages.get(0).remaining();
        int [] intPartitions = new int [partitions.length];
        for (int i = 0; i < intPartitions.length; i++) {
            intPartitions[i] = partitions[i];
        }
        List<WriteRequest []> partitionsAndWriteRequests =
                Arrays.stream(intPartitions).mapToObj(
                        intPartition -> (messages.stream().map(b -> new WriteRequest((short ) intPartition, b)).toArray(WriteRequest[]::new))
                ).collect(Collectors.toList());


        LoopThread commitThread = LoopThread.builder()
                .name(String.format("CommitThread-%s-%d", topic, partitionGroup))
                .doWork(()-> store.commit(store.rightPosition()))
                .sleepTime(0L, 0L)
                .onException(e -> logger.warn("Commit Exception: ", e))
                .build();
        commitThread.start();

        try {
            long t0 = SystemClock.now();
            int partitionIndex = 0;
            long currentBytes = 0L;
            long writeCount = 0L;

            while (currentBytes < totalBytes) {
                WriteRequest [] writeRequests = partitionsAndWriteRequests.get(partitionIndex);
                partitionIndex ++;
                if(partitionIndex >= partitionsAndWriteRequests.size()) {
                    partitionIndex = 0;
                }
                EventFuture<WriteResult> eventFuture = new EventFuture<>();
                store.asyncWrite(qosLevel, eventFuture, writeRequests);
                if(sync) {
                    eventFuture.get();
                }
                currentBytes += batchCount * size;
                writeCount += batchCount;
            }
            long t1 = SystemClock.now();

            logger.info("QOS level: {}, {}, total writes {}, takes {}ms, qps: {}, traffic: {}, msg size: {}, batch count: {}",
                    qosLevel,
                    sync ? "SYNC": "ASYNC",
                    Format.formatTraffic(currentBytes),
                    t1 - t0,
                    Format.formatWithComma(1000L * writeCount / (t1 - t0)),
                    Format.formatTraffic(1000L * currentBytes / (t1 - t0)),
                    msgSize, batchCount);

        } finally {
            commitThread.stop();
        }

    }

    @Ignore
    @Test
    public void loopTest() throws IOException, InterruptedException, ExecutionException {
        short partition = 4;
        List<ByteBuffer> messages = MessageUtils.build(1024, 1024);
        WriteRequest [] writeRequests = messages.stream().map(b -> new WriteRequest(partition, b)).toArray(WriteRequest[]::new);
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> {
                    for(;;) {
                        store.asyncWrite(QosLevel.RECEIVE, null, writeRequests);
                    }
                }),
                CompletableFuture.runAsync(() -> {
                    long index = 0 ;
                    for (;;) {
                        try {

                            index += store.read(partition, index, 1024, -1).getMessages().length;
                        } catch (PositionOverflowException ignored) {}
                        catch (Throwable e) {
                            logger.warn("ReadException: ", e);
                        }
                    }
                })
        ).get();


    }

    private void writeReadTest(QosLevel qosLevel) throws IOException, InterruptedException {
        int count = 1024;
        long timeout = 500000L;
        short partition = 4;
        List<ByteBuffer> messages = MessageUtils.build(count, 1024);


        long length = messages.stream().mapToInt(Buffer::remaining).sum();

        final EventFuture<WriteResult> future = new EventFuture<>();
        LoopThread commitThread = LoopThread.builder()
                .name(String.format("CommitThread-%s-%d", topic, partitionGroup))
                .doWork(()-> store.commit(store.rightPosition()))
                .sleepTime(0L, 10L)
                .onException(e -> logger.warn("Commit Exception: ", e))
                .build();
        commitThread.start();
        try {
            store.asyncWrite(qosLevel, future, messages.stream().map(b -> new WriteRequest(partition, b)).toArray(WriteRequest[]::new));

            WriteResult writeResult = future.get();
            Assert.assertEquals(JoyQueueCode.SUCCESS, writeResult.getCode());

            // 等待建索引都完成
            long t0 = SystemClock.now();
            while (SystemClock.now() - t0 < timeout && store.indexPosition() < length) {
                Thread.sleep(10L);
            }


            for (int i = 0; i < messages.size(); i++) {
                ByteBuffer writeBuffer = messages.get(i);
                writeBuffer.clear();

                ReadResult readResult = store.read(partition, i, 1, 0);
                Assert.assertEquals(JoyQueueCode.SUCCESS, readResult.getCode());
                Assert.assertEquals(1, readResult.getMessages().length);
                ByteBuffer readBuffer = readResult.getMessages()[0];
                Assert.assertEquals(writeBuffer, readBuffer);
            }
        } finally {
            commitThread.stop();
        }
    }
    @Test
    public void rePartitionTest() throws Exception {
        int count = 1024;
        long timeout = 500000L;
        List<ByteBuffer> messages = MessageUtils.build(count, 1024);


        long length = messages.stream().mapToInt(Buffer::remaining).sum();
        WriteRequest [] writeRequests = IntStream.range(0, messages.size())
                .mapToObj(i -> new WriteRequest(partitions[i % partitions.length], messages.get(i)))
                .toArray(WriteRequest[]::new);

        final EventFuture<WriteResult> future = new EventFuture<>();
        store.asyncWrite(QosLevel.RECEIVE, future,writeRequests);

        store.commit(store.rightPosition());

        // 等待建索引都完成
        long t0 = SystemClock.now();
        while (SystemClock.now() - t0 < timeout && store.indexPosition() < length) {
            Thread.sleep(10L);
        }

        store.rePartition(new Short[] {1, 2, 5});

        destroyStore();
        if (null == bufferPool) {
            bufferPool = PreloadBufferPool.getInstance();
            bufferPool.addPreLoad(128 * 1024 * 1024, 2, 4);
            bufferPool.addPreLoad(512 * 1024, 2, 4);
        }
        PartitionGroupStoreManager.Config config = new PartitionGroupStoreManager.Config(DEFAULT_MAX_MESSAGE_LENGTH, DEFAULT_WRITE_REQUEST_CACHE_SIZE, DEFAULT_FLUSH_INTERVAL_MS,
                DEFAULT_WRITE_TIMEOUT_MS, DEFAULT_MAX_DIRTY_SIZE, 6000,
                new PositioningStore.Config(128 * 1024 * 1024),
                new PositioningStore.Config(512 * 1024));

        this.store = new PartitionGroupStoreManager(topic, partitionGroup, groupBase, config,
                bufferPool);
        this.store.recover();
        this.store.start();
        this.store.enable();
    }

    @Test
    public void writeReadBatchMessageTest() throws IOException, InterruptedException {
        int count = 1024;
        long timeout = 500000L;
        short partition = 4;
        short batchSize = 100;
        List<ByteBuffer> messages = MessageUtils.build(count, 1024)
                .stream().map(m -> MessageUtils.toBatchMessage(m, batchSize))
                .collect(Collectors.toList());


        long length = messages.stream().mapToInt(Buffer::remaining).sum();

        final EventFuture<WriteResult> future = new EventFuture<>();
        store.asyncWrite(QosLevel.PERSISTENCE, future, messages.stream().map(b -> new WriteRequest(partition, b)).toArray(WriteRequest[]::new));


        // 等待建索引都完成
        long t0 = SystemClock.now();
        while (SystemClock.now() - t0 < timeout && store.indexPosition() < length) {
            Thread.sleep(10L);
        }

        store.commit(store.rightPosition());
        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);

            for (int j = 0; j < batchSize; j++) {
                long index = i * batchSize + j;
                ReadResult readResult = store.read(partition, index, 1, 0);
                Assert.assertEquals(JoyQueueCode.SUCCESS, readResult.getCode());
                Assert.assertEquals(1, readResult.getMessages().length);
                ByteBuffer readBuffer = readResult.getMessages()[0];
                writeBuffer.clear();
                Assert.assertEquals(writeBuffer, readBuffer);
            }

        }
    }


    @Test
    public void replicationLeaderTest() throws IOException, ExecutionException, InterruptedException {
        int count = 100;
        short partition = 4;
        List<ByteBuffer> messages = MessageUtils.build(count, 1024);

        // 注册复制回调，直接commit
        Thread replicationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                store.commit(store.flushPosition());
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        });
        replicationThread.start();
        ;

        QosStore qosStore = store.getQosStore(QosLevel.REPLICATION);
        // 写入消息
        Future<WriteResult> future = qosStore.asyncWrite(messages.stream().map(b -> new WriteRequest(partition, b)).toArray(WriteRequest[]::new));
        // 等待写入完成
        WriteResult writeResult = future.get();

        replicationThread.interrupt();
        // 验证结果
        Assert.assertEquals(JoyQueueCode.SUCCESS, writeResult.getCode());
        Assert.assertEquals(count, writeResult.getIndices().length);

        while (store.indexPosition() < store.rightPosition()) {
            Thread.sleep(1);
        }
        store.commit(store.rightPosition());

        // 读消息，并验证
        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);
            writeBuffer.clear();

            ReadResult readResult = store.read(partition, i, 1, 0);
            Assert.assertEquals(JoyQueueCode.SUCCESS, readResult.getCode());
            Assert.assertEquals(1, readResult.getMessages().length);
            ByteBuffer readBuffer = readResult.getMessages()[0];
            Assert.assertEquals(writeBuffer, readBuffer);
        }
    }

    @Test
    public void replicationOverflowTest() throws Exception {
        int repeatCount = 20; // 100 * 10MB = 1GB
        long index = 0L;
        List<ByteBuffer> messages = MessageUtils.build(10240, 1024);
        ByteBuffer replicationMessages = ByteBuffer.allocate(messages.stream().mapToInt(Buffer::remaining).sum());

        for (int i = 0; i < repeatCount; i++) {
            replicationMessages.clear();
            for (ByteBuffer byteBuffer : messages) {
                // 分区
                MessageParser.setShort(byteBuffer, MessageParser.PARTITION, partitions[0]);
                // 轮次
                MessageParser.setInt(byteBuffer, MessageParser.TERM, 0);
                // 存储时间：与发送时间的差值
                MessageParser.setInt(byteBuffer, MessageParser.STORAGE_TIMESTAMP,
                        (int) (SystemClock.now() - MessageParser.getLong(byteBuffer, MessageParser.CLIENT_TIMESTAMP)));
                // 索引
                MessageParser.setLong(byteBuffer, MessageParser.INDEX, index++);
                replicationMessages.put(byteBuffer);
                byteBuffer.clear();
            }

            replicationMessages.flip();
            store.disable();
            boolean repeat = true;
            while (repeat)
                try {
                    store.appendEntryBuffer(replicationMessages);
                    repeat = false;
                } catch (TimeoutException ignored) {
                }
            logger.info("Index: {}, {}...", index, Format.formatSize(index * 1024));
        }


    }

    @Test
    public void replicationReadWriteTest() throws Exception {
        long timeout = 500000L;

        int termCount = 3;
        short partition = 4;
        int msgCountEachTerm = 128;
        List<ByteBuffer> messages = MessageUtils.build(msgCountEachTerm, 1111);
        messages.addAll(MessageUtils.build((termCount - 1) * msgCountEachTerm, 1113));
        long size = messages.stream().mapToInt(ByteBuffer::remaining).sum();
        IntStream.range(0, termCount)
                .forEach(term -> IntStream.range(0, msgCountEachTerm)
                        .forEach(i -> MessageParser.setInt(messages.get(term * msgCountEachTerm + i), MessageParser.TERM, term)));


        // 1. 生产消息

        QosStore qosStore = store.getQosStore(QosLevel.PERSISTENCE);
        // 写入消息
        Future<WriteResult> future = qosStore.asyncWrite(messages.stream().map(b -> new WriteRequest(partition, b)).toArray(WriteRequest[]::new));
        // 等待写入完成
        WriteResult writeResult = future.get();
        Assert.assertEquals(JoyQueueCode.SUCCESS, writeResult.getCode());
        Assert.assertEquals(size, store.rightPosition());
        // 2. 模拟LEADER读取消息
        List<ByteBuffer> readTermBuffers = new LinkedList<>();

        long position = 0L;
        while (position < store.rightPosition()) {
            ByteBuffer readBuffer = store.readEntryBuffer(position, 10 * 1024);
            position += readBuffer.remaining();

            verifyTerm(readBuffer);


            readTermBuffers.add(readBuffer);
        }

        destroyStore();
        destroyBaseDir();
        prepareBaseDir();
        initAndRecoverStore();
        store.disable();
        // 3. 模拟FOLLOWER 写入消息
        position = 0L;
        for (ByteBuffer termBuffer : readTermBuffers) {
            position = store.appendEntryBuffer(termBuffer);
        }

        Assert.assertEquals(size, position);
        Assert.assertEquals(size, store.rightPosition());
        store.commit(store.rightPosition());

        // 等待索引创建完成
        while (store.indexPosition() < store.rightPosition()) {
            Thread.sleep(10);
        }

        // 4. 消费消息，并验证

        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);
            writeBuffer.clear();

            ReadResult readResult = store.read(partition, i, 1, 0);
            Assert.assertEquals(JoyQueueCode.SUCCESS, readResult.getCode());
            Assert.assertEquals(1, readResult.getMessages().length);
            ByteBuffer readBuffer = readResult.getMessages()[0];
            Assert.assertEquals(writeBuffer, readBuffer);
        }


    }

    @Test
    public void brokenIndexStoreTest() throws Exception{
        int count = 55;
        long timeout = 500000L;
        short partition = 4;
        List<ByteBuffer> messages = MessageUtils.build(count, 1024);


        long length = messages.stream().mapToInt(Buffer::remaining).sum();

        final EventFuture<WriteResult> future = new EventFuture<>();
        store.asyncWrite(QosLevel.RECEIVE, future, messages.stream().map(b -> new WriteRequest(partition, b)).toArray(WriteRequest[]::new));


        // 等待建索引都完成
        long t0 = SystemClock.now();
        while (SystemClock.now() - t0 < timeout && store.indexPosition() < length) {
            Thread.sleep(10L);
        }

        store.disable();
        store.stop();
        store.close();
        store = null;

        File indexBase = new File(groupBase, "index/" + partitions[0]);

        File[] files = indexBase.listFiles(file -> file.isFile() && file.getName().matches("\\d+"));

        File lastFile = new File(indexBase, String.valueOf(Arrays.stream(files).mapToLong(file -> Long.parseLong(file.getName())).max().orElse(0L)));

        byte [] zeros = new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        FileOutputStream output = new FileOutputStream(lastFile,true);
        try {
            output.write(zeros);
            output.flush();
        } finally {
            output.close();
        }

        store = new PartitionGroupStoreManager(topic, partitionGroup, groupBase, new PartitionGroupStoreManager.Config(),
                bufferPool);
        store.recover();
        store.start();
        store.enable();
        store.commit(store.rightPosition());
        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);
            writeBuffer.clear();

            ReadResult readResult = store.read(partition, i, 1, 0);
            Assert.assertEquals(JoyQueueCode.SUCCESS, readResult.getCode());
            Assert.assertEquals(1, readResult.getMessages().length);
            ByteBuffer readBuffer = readResult.getMessages()[0];
            Assert.assertEquals(writeBuffer, readBuffer);
        }
    }

    @Test
    public void getIndexTest() throws InterruptedException, IOException {
        long timeout = 500000L;
        List<ByteBuffer> msgs = MessageUtils.build(20, 255);
        long startTime = SystemClock.now();
        short partition = partitions[0];
        final EventFuture<WriteResult> future = new EventFuture<>();

        for (int i = 0; i < 20; i++) {
            ByteBuffer msg = msgs.get(i);
            MessageParser.setLong(msg, MessageParser.CLIENT_TIMESTAMP, startTime);
            MessageParser.setShort(msg, MessageParser.PARTITION, partition);
            if (i < 10 || i >= 15) {
                store.asyncWrite(QosLevel.PERSISTENCE, future, new WriteRequest(partition, msgs.get(i)));
            } else if (i == 10) {
                store.asyncWrite(QosLevel.PERSISTENCE, future, new WriteRequest(partition, MessageUtils.toBatchMessage(msgs.get(i), (short) 5)));
            }
            Thread.sleep(100L);

        }

        store.commit(store.rightPosition());

        // 等待建索引都完成
        long t0 = SystemClock.now();
        while (SystemClock.now() - t0 < timeout && store.getRightIndex(partition) < 20) {
            Thread.sleep(10L);
        }

        long[] storageTimestamps = Arrays.stream(store.read(partition, 0L, 20, 0).getMessages())
                .mapToLong(b -> MessageParser.getInt(b, MessageParser.STORAGE_TIMESTAMP)).toArray();


        Assert.assertEquals(0L, store.getIndex(partition, startTime - 1L));
        Assert.assertEquals(0L, store.getIndex(partition, startTime + storageTimestamps[0]));
        Assert.assertEquals(4L, store.getIndex(partition, startTime + storageTimestamps[3] + 10L));
        Assert.assertEquals(4L, store.getIndex(partition, startTime + storageTimestamps[4]));
        Assert.assertEquals(10L, store.getIndex(partition, startTime + storageTimestamps[10]));
        Assert.assertEquals(15L, store.getIndex(partition, startTime + storageTimestamps[10] + 10L));
        Assert.assertEquals(19L, store.getIndex(partition, startTime + storageTimestamps[19 - 4]));
        Assert.assertEquals(-1L, store.getIndex(partition, startTime + storageTimestamps[19 - 4] + 10L));


    }

    private void verifyTerm(ByteBuffer buffer) {
        int term = -1;
        ByteBuffer r = buffer.asReadOnlyBuffer();
        while (r.hasRemaining()) {
            if (term >= 0) {
                Assert.assertEquals(term, MessageParser.getInt(r, MessageParser.TERM));
            } else {
                term = MessageParser.getInt(r, MessageParser.TERM);
            }
            r.position(r.position() + MessageParser.getInt(r, MessageParser.LENGTH));
        }
    }
    @Test
    public void checkpointTest() throws Exception {
        int count = 1024;
        short partition = 4;
        List<ByteBuffer> messages = MessageUtils.build(count, 1024);

        long length = messages.stream().mapToInt(Buffer::remaining).sum();

        final EventFuture<WriteResult> future = new EventFuture<>();

        store.asyncWrite(QosLevel.PERSISTENCE, future, messages.stream().map(b -> new WriteRequest(partition, b)).toArray(WriteRequest[]::new));

        WriteResult writeResult = future.get();
        Assert.assertEquals(JoyQueueCode.SUCCESS, writeResult.getCode());

        logger.info("Waiting checkpoint saved...");

        File checkpointFile = new File(groupBase, PartitionGroupStoreManager.CHECKPOINT_FILE);

        while (!checkpointFile.isFile()) {
            Thread.sleep(100L);
        }
        Thread.sleep(100L);

        destroyStore();
        recoverStore();
        store.commit(store.rightPosition());
        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);
            writeBuffer.clear();

            ReadResult readResult = store.read(partition, i, 1, 0);
            Assert.assertEquals(JoyQueueCode.SUCCESS, readResult.getCode());
            Assert.assertEquals(1, readResult.getMessages().length);
            ByteBuffer readBuffer = readResult.getMessages()[0];
            Assert.assertEquals(writeBuffer, readBuffer);
        }


    }

    @Before
    public void before() throws Exception {
        prepareBaseDir();
        initAndRecoverStore();
    }

    @After
    public void after() throws Exception {
        destroyStore();
        destroyBaseDir();
    }

    private void destroyBaseDir() {
        BaseDirUtils.destroyBaseDir(base);

        groupBase = null;
        base = null;
    }

    private void prepareBaseDir() throws IOException {

        base = BaseDirUtils.prepareBaseDir();
        logger.info("Base directory: {}.", base.getCanonicalPath());

        groupBase = new File(base, String.format("%s/%d", topic, partitionGroup));
        logger.info("Partition Group base directory: {}.", groupBase.getCanonicalPath());

    }

    private void initAndRecoverStore() throws Exception {

        PartitionGroupStoreSupport.init(groupBase, partitions);

        recoverStore();
    }

    private void recoverStore() {
        if (null == bufferPool) {
            bufferPool = PreloadBufferPool.getInstance();
            bufferPool.addPreLoad(128 * 1024 * 1024, 2, 4);
            bufferPool.addPreLoad(512 * 1024, 2, 4);
        }

        PartitionGroupStoreManager.Config config = new PartitionGroupStoreManager.Config(
                DEFAULT_MAX_MESSAGE_LENGTH,
                DEFAULT_WRITE_REQUEST_CACHE_SIZE,
                1L,
                DEFAULT_WRITE_TIMEOUT_MS, DEFAULT_MAX_DIRTY_SIZE, 6000,
                new PositioningStore.Config(128 * 1024 * 1024),
                new PositioningStore.Config(512 * 1024));

        this.store = new PartitionGroupStoreManager(topic, partitionGroup, groupBase, config,
                bufferPool);
        this.store.recover();
        this.store.start();
        this.store.enable();
    }

    private void destroyStore() {
        if (this.store != null) {
            store.disable();
            store.stop();
            store.close();
            store = null;
        }
        if (null != bufferPool) {
            bufferPool = null;
        }
    }

}
