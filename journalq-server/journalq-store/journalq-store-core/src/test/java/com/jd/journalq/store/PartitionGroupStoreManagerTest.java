package com.jd.journalq.store;

import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.store.message.MessageParser;
import com.jd.journalq.store.nsm.VirtualThreadExecutor;
import com.jd.journalq.store.utils.BaseDirUtils;
import com.jd.journalq.store.utils.MessageUtils;
import com.jd.journalq.store.utils.PreloadBufferPool;
import com.jd.journalq.toolkit.concurrent.EventFuture;
import com.jd.journalq.toolkit.format.Format;
import com.jd.journalq.toolkit.time.SystemClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author liyue25
 * Date: 2018/8/30
 */
public class PartitionGroupStoreManagerTest {
    private static final Logger logger = LoggerFactory.getLogger(PartitionGroupStoreManagerTest.class);

    private File base = null;
    private File groupBase = null;
    private final static String topic = "test_topic";
    private final static int partitionGroup = 3;
    private final static short [] partitions = new short [] {4,5,6};
    private final static int [] nodes = new int[] {0};
    private PartitionGroupStoreManager store;
    private VirtualThreadExecutor virtualThreadPool;
    private PreloadBufferPool bufferPool;

    @Test
    public void writeReadTest() throws Exception {
        writeReadTest(QosLevel.PERSISTENCE);
        after();
        before();
        writeReadTest(QosLevel.RECEIVE);

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
            store.asyncWrite(QosLevel.ONE_WAY, null,new WriteRequest(partitions[ThreadLocalRandom.current().nextInt(partitions.length)],msg));
        }
        // 等待建索引都完成
        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < timeout && store.rightPosition() < length){
            Thread.sleep(10L);
        }

        for (int i = 0; i < partitions.length; i++) {

            for (long j = 0; j < store.getRightIndex(partitions[i]); j++) {
                ReadResult readResult  = store.read(partitions[i], j, 1, 0);
                Assert.assertEquals(JMQCode.SUCCESS,readResult.getCode());
                Assert.assertEquals(1,readResult.getMessages().length);
                ByteBuffer readBuffer = readResult.getMessages()[0];
                Assert.assertEquals(readBuffer.getInt(0), readBuffer.remaining());
                length -= readBuffer.remaining();
            }
        }

        Assert.assertEquals(0L, length);

    }

    private void writeReadTest(QosLevel qosLevel) throws IOException, InterruptedException {
        int count  =  1024;
        long timeout = 500000L;
        short partition = 4;
        List<ByteBuffer> messages = MessageUtils.build(count,1024);


        long length = messages.stream().mapToInt(Buffer::remaining).sum();

        final EventFuture<WriteResult> future = new EventFuture<>();
        store.asyncWrite(qosLevel, future,messages.stream().map(b->new WriteRequest(partition, b)).toArray(WriteRequest[]::new));


        // 等待建索引都完成
        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < timeout && store.indexPosition() < length){
            Thread.sleep(10L);
        }

        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);
            writeBuffer.clear();

            ReadResult readResult = store.read(partition,i,1,0);
            Assert.assertEquals(JMQCode.SUCCESS,readResult.getCode());
            Assert.assertEquals(1,readResult.getMessages().length);
            ByteBuffer readBuffer = readResult.getMessages()[0];
            Assert.assertEquals(writeBuffer,readBuffer);
        }
    }

    @Test
    public void writeReadBatchMessageTest() throws IOException, InterruptedException {
        int count  =  1024;
        long timeout = 500000L;
        short partition = 4;
        short batchSize = 100;
        List<ByteBuffer> messages = MessageUtils.build(count,1024)
                .stream().map(m -> MessageUtils.toBatchMessage(m, batchSize))
                .collect(Collectors.toList());


        long length = messages.stream().mapToInt(Buffer::remaining).sum();

        final EventFuture<WriteResult> future = new EventFuture<>();
        store.asyncWrite(QosLevel.PERSISTENCE, future,messages.stream().map(b->new WriteRequest(partition, b)).toArray(WriteRequest[]::new));


        // 等待建索引都完成
        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < timeout && store.indexPosition() < length){
            Thread.sleep(10L);
        }

        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);

            for (int j = 0; j < batchSize; j++) {
                long index = i * batchSize + j;
                ReadResult readResult = store.read(partition,index,1,0);
                Assert.assertEquals(JMQCode.SUCCESS,readResult.getCode());
                Assert.assertEquals(1,readResult.getMessages().length);
                ByteBuffer readBuffer = readResult.getMessages()[0];
                writeBuffer.clear();
                Assert.assertEquals(writeBuffer,readBuffer);
            }

        }
    }



    @Test
    public void replicationLeaderTest() throws IOException, ExecutionException, InterruptedException {
        int count  =  100;
        short partition = 4;
        List<ByteBuffer> messages = MessageUtils.build(count,1024);

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
        replicationThread.start();;

        QosStore qosStore = store.getQosStore(QosLevel.REPLICATION);
        // 写入消息
        Future<WriteResult> future = qosStore.asyncWrite(messages.stream().map(b->new WriteRequest(partition, b)).toArray(WriteRequest[]::new));
        // 等待写入完成
        WriteResult writeResult = future.get();

        replicationThread.interrupt();
        // 验证结果
        Assert.assertEquals(JMQCode.SUCCESS, writeResult.getCode());
        Assert.assertEquals(count, writeResult.getIndices().length);

        while (store.indexPosition() < store.rightPosition()) {
            Thread.sleep(1);
        }

        // 读消息，并验证
        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);
            writeBuffer.clear();

            ReadResult readResult = store.read(partition,i,1,0);
            Assert.assertEquals(JMQCode.SUCCESS,readResult.getCode());
            Assert.assertEquals(1,readResult.getMessages().length);
            ByteBuffer readBuffer = readResult.getMessages()[0];
            Assert.assertEquals(writeBuffer,readBuffer);
        }
    }

    @Test
    public void replicationOverflowTest() throws Exception {
        int repeatCount = 20; // 100 * 10MB = 1GB
        long index = 0L;
        List<ByteBuffer> messages = MessageUtils.build(10240,  1024);
        ByteBuffer replicationMessages = ByteBuffer.allocate(messages.stream().mapToInt(Buffer::remaining).sum());

        for(int i= 0; i< repeatCount;i++) {
            replicationMessages.clear();
            for(ByteBuffer byteBuffer: messages) {
                // 分区
                MessageParser.setShort(byteBuffer, MessageParser.PARTITION, partitions[0]);
                // 轮次
                MessageParser.setInt(byteBuffer, MessageParser.TERM, 0);
                // 存储时间：与发送时间的差值
                MessageParser.setInt(byteBuffer, MessageParser.STORAGE_TIMESTAMP,
                        (int )(SystemClock.now() - MessageParser.getLong(byteBuffer, MessageParser.CLIENT_TIMESTAMP)));
                // 索引
                MessageParser.setLong(byteBuffer, MessageParser.INDEX, index++);
                replicationMessages.put(byteBuffer);
                byteBuffer.clear();
            }

            replicationMessages.flip();

            boolean repeat = true;
            while (repeat)
                try {
                    store.appendEntryBuffer(replicationMessages);
                    repeat = false;
                } catch (TimeoutException ignored) {}
            logger.info("Index: {}, {}...", index, Format.formatTraffic(index * 1024));
        }


    }

    @Test
    public void replicationReadWriteTest() throws Exception{
        long timeout = 500000L;

        int termCount = 3;
        short partition = 4;
        int msgCountEachTerm = 128;
        List<ByteBuffer> messages = MessageUtils.build(msgCountEachTerm,1111);
        messages.addAll(MessageUtils.build((termCount -1 ) * msgCountEachTerm,1113));
        long size = messages.stream().mapToInt(ByteBuffer::remaining).sum();
        IntStream.range(0, termCount)
                .forEach(term -> IntStream.range(0, msgCountEachTerm)
                        .forEach(i -> MessageParser.setInt(messages.get(term * msgCountEachTerm + i), MessageParser.TERM, term)));



        // 1. 生产消息

        QosStore qosStore = store.getQosStore(QosLevel.PERSISTENCE);
        // 写入消息
        Future<WriteResult> future = qosStore.asyncWrite(messages.stream().map(b->new WriteRequest(partition, b)).toArray(WriteRequest[]::new));
        // 等待写入完成
        WriteResult writeResult = future.get();
        Assert.assertEquals(JMQCode.SUCCESS, writeResult.getCode());
        Assert.assertEquals(size, store.rightPosition());
        // 2. 模拟LEADER读取消息
        List<ByteBuffer> readTermBuffers = new LinkedList<>();

        long position = 0L;
        while (position < store.rightPosition()){
            ByteBuffer readBuffer = store.readEntryBuffer(position, 10 * 1024);
            position += readBuffer.remaining();

            verifyTerm(readBuffer);


            readTermBuffers.add(readBuffer);
        }

        destroyStore();
        destroyBaseDir();
        prepareBaseDir();
        prepareStore();

        // 3. 模拟FOLLOWER 写入消息
        position = 0L;
        for(ByteBuffer termBuffer: readTermBuffers) {
            position = store.appendEntryBuffer(termBuffer);
        }

        Assert.assertEquals(size, position);
        Assert.assertEquals(size, store.rightPosition());

        // 等待索引创建完成
        while (store.indexPosition() < store.rightPosition()) {
            Thread.sleep(10);
        }

        // 4. 消费消息，并验证

        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);
            writeBuffer.clear();

            ReadResult readResult = store.read(partition,i,1,0);
            Assert.assertEquals(JMQCode.SUCCESS,readResult.getCode());
            Assert.assertEquals(1,readResult.getMessages().length);
            ByteBuffer readBuffer = readResult.getMessages()[0];
            Assert.assertEquals(writeBuffer,readBuffer);
        }



    }
    @Test
    public void getIndexTest() throws InterruptedException, IOException {
        long timeout = 500000L;
        List<ByteBuffer> msgs = MessageUtils.build(20, 255);
        long startTime = System.currentTimeMillis();
        short partition = partitions[0];
        final EventFuture<WriteResult> future = new EventFuture<>();

        for (int i = 0; i < 20; i++) {
            ByteBuffer msg = msgs.get(i);
            MessageParser.setLong(msg, MessageParser.CLIENT_TIMESTAMP, startTime);
            MessageParser.setShort(msg, MessageParser.PARTITION, partition);
            if (i < 10 || i >= 15) {
                store.asyncWrite(QosLevel.PERSISTENCE, future,new WriteRequest(partition,msgs.get(i)));
            } else if(i == 10) {
                store.asyncWrite(QosLevel.PERSISTENCE, future,new WriteRequest(partition,MessageUtils.toBatchMessage(msgs.get(i), (short) 5)));
            }
            Thread.sleep(100L);

        }




        // 等待建索引都完成
        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < timeout && store.getRightIndex(partition) < 20){
            Thread.sleep(10L);
        }

        long [] storageTimestamps = Arrays.stream(store.read(partition, 0L, 20, 0).getMessages())
                .mapToLong(b -> MessageParser.getInt(b, MessageParser.STORAGE_TIMESTAMP)).toArray();


        Assert.assertEquals(0L, store.getIndex(partition, startTime - 1L));
        Assert.assertEquals(0L, store.getIndex(partition, startTime + storageTimestamps[0]));
        Assert.assertEquals(4L, store.getIndex(partition, startTime + storageTimestamps[3] + 10L));
        Assert.assertEquals(4L, store.getIndex(partition, startTime  + storageTimestamps[4]));
        Assert.assertEquals(10L, store.getIndex(partition, startTime + storageTimestamps[10]));
        Assert.assertEquals(15L, store.getIndex(partition, startTime + storageTimestamps[10] + 10L));
        Assert.assertEquals(19L, store.getIndex(partition, startTime + storageTimestamps[19 - 4]));
        Assert.assertEquals(-1L, store.getIndex(partition, startTime + storageTimestamps[19 - 4] + 10L));


    }

    private void verifyTerm(ByteBuffer buffer) {
        int term = -1;
        ByteBuffer r = buffer.asReadOnlyBuffer();
        while (r.hasRemaining()) {
            if(term >= 0) {
                Assert.assertEquals(term, MessageParser.getInt(r,MessageParser.TERM));
            } else {
                term = MessageParser.getInt(r,MessageParser.TERM);
            }
            r.position(r.position() +MessageParser.getInt(r,MessageParser.LENGTH));
        }
    }

    private long getPositionByQosLevel(QosLevel qosLevel, PartitionGroupStoreManager store) {
        switch (qosLevel) {

            case PERSISTENCE: return store.rightPosition();
            case REPLICATION: return store.commitPosition();
            default: return store.rightPosition();
        }
    }




    private void enrich(ByteBuffer byteBuffer, short partition, long index, int term){

        // 分区
        MessageParser.setShort(byteBuffer, MessageParser.PARTITION, partition);
        // 索引
        MessageParser.setLong(byteBuffer, MessageParser.INDEX, index);

        // 轮次
        MessageParser.setInt(byteBuffer, MessageParser.TERM, term);

        // 存储时间：与发送时间的差值
        MessageParser.setInt(byteBuffer, MessageParser.STORAGE_TIMESTAMP,
                (int )(SystemClock.now() - MessageParser.getLong(byteBuffer, MessageParser.CLIENT_TIMESTAMP)));

    }
    @Before
    public void before() throws Exception {
        prepareBaseDir();
        prepareStore();
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

        groupBase = new File(base,String.format("%s/%d", topic,partitionGroup));
        logger.info("Partition Group base directory: {}.", groupBase.getCanonicalPath());

    }

    private void prepareStore() throws Exception {

        PartitionGroupStoreSupport.init(groupBase,partitions);
        if(null == virtualThreadPool) virtualThreadPool = new VirtualThreadExecutor(500, 100,10, 1000,4);
        if(null == bufferPool) {
            bufferPool = new PreloadBufferPool( );
            bufferPool.addPreLoad(128 * 1024 * 1024, 2, 4);
            bufferPool.addPreLoad(10 * 1024 * 1024, 2, 4);
        }

        this.store = new PartitionGroupStoreManager(topic, partitionGroup, groupBase,new PartitionGroupStoreManager.Config(),
                bufferPool,
                Executors.newSingleThreadScheduledExecutor(),
                virtualThreadPool);
        this.store.recover();
        this.store.start();
        this.store.enable();
    }

    private void destroyStore() throws IOException {
        if(this.store != null) {
            store.disable(5000L);
            store.stop();
            store.close();
            store = null;
        }
        if(null != virtualThreadPool) {
            virtualThreadPool.stop();
            virtualThreadPool = null;
        }

        if(null != bufferPool) {
            bufferPool.close();
            bufferPool = null;
        }
    }

}
