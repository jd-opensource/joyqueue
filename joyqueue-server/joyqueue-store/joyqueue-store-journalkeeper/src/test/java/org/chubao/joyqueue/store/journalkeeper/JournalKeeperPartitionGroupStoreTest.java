/**
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
package org.chubao.joyqueue.store.journalkeeper;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.store.PositionOverflowException;
import org.joyqueue.store.ReadResult;
import org.joyqueue.store.WriteRequest;
import org.joyqueue.store.WriteResult;
import org.joyqueue.store.message.MessageParser;
import org.joyqueue.toolkit.concurrent.EventFuture;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.format.Format;
import org.joyqueue.toolkit.time.SystemClock;
import org.joyqueue.toolkit.util.BaseDirUtils;
import io.journalkeeper.core.api.RaftServer;
import io.journalkeeper.rpc.URIParser;
import io.journalkeeper.utils.event.EventType;
import io.journalkeeper.utils.spi.ServiceSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


/**
 * @author liyue25
 * Date: 2018/8/30
 */
public class JournalKeeperPartitionGroupStoreTest {
    private static final Logger logger = LoggerFactory.getLogger(JournalKeeperPartitionGroupStoreTest.class);
    private static final String topic = "test_topic";
    private static final int partitionGroup = 3;
    private static final short[] partitions = new short[]{4, 5, 6};
    private File base = null;
    private File groupBase = null;
    private static final List<Integer> brokerIds = Arrays.asList(88886,88887,88888);
    private JournalKeeperPartitionGroupStore leaderStore;
    private List<JournalKeeperPartitionGroupStore> stores = new ArrayList<>(brokerIds.size());
    private final JoyQueueTestUriParser joyQueueUriParser =
            ServiceSupport.load(URIParser.class, JoyQueueTestUriParser.class.getCanonicalName());
    private ExecutorService asyncExecutor;
    private ScheduledExecutorService scheduledExecutor;


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
        int count = 10240 ;
        long length = 0L;

        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int bodySize = ThreadLocalRandom.current().nextInt(127) + 1;
            
            ByteBuffer msg = MessageUtils.build(1, bodySize).get(0);
            length += msg.remaining();
            leaderStore.asyncWrite(e -> latch.countDown(), QosLevel.REPLICATION, new WriteRequest(partitions[ThreadLocalRandom.current().nextInt(partitions.length)], msg));
        }
        // 等待写入成功
        latch.await();

        for (short partition : partitions) {

            for (long j = 0; j < leaderStore.getRightIndex(partition); j++) {
                ReadResult readResult = leaderStore.read(partition, j, 1, 0);
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
            leaderStore.asyncWrite(eventFuture, qosLevel, writeRequests);
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
    }

    @Ignore
    @Test
    public void loopTest() throws InterruptedException, ExecutionException {
        short partition = 4;
        List<ByteBuffer> messages = MessageUtils.build(1024, 1024);
        WriteRequest [] writeRequests = messages.stream().map(b -> new WriteRequest(partition, b)).toArray(WriteRequest[]::new);
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> {
                    for(;;) {
                        leaderStore.asyncWrite(QosLevel.RECEIVE, writeRequests);
                    }
                }),
                CompletableFuture.runAsync(() -> {
                    long index = 0 ;
                    for (;;) {
                        try {

                            index += leaderStore.read(partition, index, 1024, -1).getMessages().length;
                        } catch (PositionOverflowException ignored) {}
                        catch (Throwable e) {
                            logger.warn("ReadException: ", e);
                        }
                    }
                })
        ).get();


    }

    private void writeReadTest(QosLevel qosLevel) throws InterruptedException {
        int count = 1024;
        short partition = 5;
        long timeout = 500000L;
        List<ByteBuffer> messages = MessageUtils.build(count, 1024);


        for (ByteBuffer message : messages) {
            final EventFuture<WriteResult> future = new EventFuture<>();
            leaderStore.asyncWrite(future, qosLevel,   new WriteRequest(partition, message));
            WriteResult writeResult = future.get();
            Assert.assertEquals(JoyQueueCode.SUCCESS, writeResult.getCode());
        }



        // 等待建索引都完成
        long t0 = SystemClock.now();
        while (SystemClock.now() - t0 < timeout && leaderStore.getRightIndex(partition) < count) {
            Thread.sleep(10L);
        }

        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i).slice();
            writeBuffer.clear();
            writeBuffer.position(MessageParser.getFixedAttributesLength());

            ReadResult readResult = leaderStore.read(partition, i, 1, 0);
            Assert.assertEquals(JoyQueueCode.SUCCESS, readResult.getCode());
            Assert.assertEquals(1, readResult.getMessages().length);
            ByteBuffer readBuffer = readResult.getMessages()[0];
            readBuffer.position(MessageParser.getFixedAttributesLength());
            Assert.assertEquals(writeBuffer, readBuffer);
        }

    }


    @Test
    public void writeReadBatchMessageTest() throws IOException, InterruptedException {
        int count = 1024;
        short partition = 4;
        short batchSize = 100;
        List<ByteBuffer> messages = MessageUtils.build(count, 1024)
                .stream().map(m -> MessageUtils.toBatchMessage(m, batchSize))
                .collect(Collectors.toList());



        final EventFuture<WriteResult> future = new EventFuture<>();
        leaderStore.asyncWrite(future, QosLevel.REPLICATION, messages.stream().map(b -> new WriteRequest(partition, b, batchSize)).toArray(WriteRequest[]::new));

        WriteResult writeResult = future.get();
        Assert.assertEquals(JoyQueueCode.SUCCESS, writeResult.getCode());

        Assert.assertEquals((long) batchSize * count, leaderStore.getRightIndex(partition));
        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer writeBuffer = messages.get(i);
            byte [] expectedBody = MessageParser.getBytes(writeBuffer, MessageParser.BODY);
            for (int j = 0; j < batchSize; j++) {
                long index = i * batchSize + j;
                ReadResult readResult = leaderStore.read(partition, index, 1, 0);
                Assert.assertEquals(JoyQueueCode.SUCCESS, readResult.getCode());
                Assert.assertEquals(1, readResult.getMessages().length);
                ByteBuffer readBuffer = readResult.getMessages()[0];
                byte [] readBody = MessageParser.getBytes(readBuffer, MessageParser.BODY);
                writeBuffer.clear();

                Assert.assertArrayEquals(expectedBody, readBody);
            }

        }
    }

    @Before
    public void before() throws Exception {
        scheduledExecutor = Executors.newScheduledThreadPool(4, new NamedThreadFactory("Store-Scheduled-Executor"));
        asyncExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("Store-Async-Executor"));

        prepareBaseDir();
        prepareStore();

    }

    @After
    public void after() throws Exception {

        destroyStore();
        destroyBaseDir();
        if(null != asyncExecutor) {
            asyncExecutor.shutdownNow();
        }
        if(null != scheduledExecutor) {
            scheduledExecutor.shutdownNow();
        }

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
    private String getPartitionGroupRelPath(String topic, int partitionGroup) {
        return "topics" + File.separator + topic.replace('/', '@') + File.separator + partitionGroup;
    }

    private List<URI> toURIs(List<Integer> brokerIds, String topic, int group) {
        return brokerIds.stream()
                .map(brokerId -> toURI(brokerId, topic, group))
                .collect(Collectors.toList());
    }

    private URI toURI(int brokerId, String topic, int group) {
        return joyQueueUriParser.create(topic, group, brokerId);
    }


    private void prepareStore() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        for (int brokerId : brokerIds) {
            File groupBase = new File(new File(base, String.valueOf(brokerId)), getPartitionGroupRelPath(topic, partitionGroup));
            Properties properties = new Properties();
            properties.setProperty("working_dir", groupBase.getAbsolutePath());
            JournalKeeperPartitionGroupStore store =
                    new JournalKeeperPartitionGroupStore(
                            topic,
                            partitionGroup,
                            RaftServer.Roll.VOTER,
                            event -> {
                                if (event.getEventType() == EventType.ON_LEADER_CHANGE ) {
                                    String leaderUri = event.getEventData().get("leader");
                                    JournalKeeperPartitionGroupStore leader = stores.stream()
                                            .filter(store1 -> store1.getUri().equals(URI.create(leaderUri)))
                                            .findAny().orElse(null);
                                    if(null != leader) {
                                        leaderStore = leader;
                                        latch.countDown();
                                    }

                                }
                            },
                            asyncExecutor, scheduledExecutor, properties);
            store.init(toURIs(brokerIds , topic, partitionGroup), toURI(brokerId, topic, partitionGroup), partitions);
            store.restore();
            stores.add(store);
            store.start();
        }

        latch.await();
    }

    private void destroyStore() {
        for (JournalKeeperPartitionGroupStore store : stores) {
            if(null != store) {
                store.stop();
            }
        }
        stores.clear();
        if (this.leaderStore != null) {
            leaderStore = null;
        }
    }

}
