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
package io.chubao.joyqueue.store;

import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.store.file.DiskFullException;
import io.chubao.joyqueue.store.file.PositioningStore;
import io.chubao.joyqueue.store.file.RollBackException;
import io.chubao.joyqueue.store.file.StoreMessageSerializer;
import io.chubao.joyqueue.store.file.WriteException;
import io.chubao.joyqueue.store.index.IndexItem;
import io.chubao.joyqueue.store.index.IndexSerializer;
import io.chubao.joyqueue.store.message.BatchMessageParser;
import io.chubao.joyqueue.store.message.MessageParser;
import io.chubao.joyqueue.store.nsm.VirtualThread;
import io.chubao.joyqueue.store.nsm.VirtualThreadExecutor;
import io.chubao.joyqueue.store.replication.ReplicableStore;
import io.chubao.joyqueue.store.utils.PreloadBufferPool;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.concurrent.LoopThread;
import io.chubao.joyqueue.toolkit.format.Format;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;
import io.chubao.joyqueue.toolkit.metric.Metric;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liyue25
 * Date: 2018/8/13
 */
public class PartitionGroupStoreManager implements ReplicableStore, LifeCycle, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PartitionGroupStoreManager.class);
    private static final long EVENT_TIMEOUT_MILLS = 60 * 1000L;
    private final PositioningStore<ByteBuffer> store;
    private final File base;
    private final String topic;
    private final int partitionGroup;
    private final CallbackPositioningBelt flushCallbackBelt, commitCallbackBelt;
    private final Map<Short, Partition> partitionMap = new ConcurrentHashMap<>();
    private final Config config;
    private final QosStore[] qosStores =
            {new QosStore(this, QosLevel.ONE_WAY),
                    new QosStore(this, QosLevel.RECEIVE),
                    new QosStore(this, QosLevel.PERSISTENCE),
                    new QosStore(this, QosLevel.REPLICATION)
            };
    private final ScheduledExecutorService scheduledExecutorService;
    private final PreloadBufferPool bufferPool;
    private final VirtualThreadExecutor virtualThreadPool;
    private final VirtualThread callbackVirtualThread = this::callbackVT;
    private final LoopThread writeLoopThread, flushLoopThread;
    private final LoopThread metricThread;
    private final BlockingQueue<WriteCommand> writeCommandCache;
    private long replicationPosition;
    private long indexPosition;
    private final VirtualThread writeVirtualThread = this::writeVT;
    private AtomicBoolean started, enabled;
    private int term; // 当前轮次
    private Metric produceMetrics = null, consumeMetrics = null;
    private Metric.MetricInstance produceMetric = null, consumeMetric;
    private ScheduledFuture callbackFeature;
    private final Lock writeLock = new ReentrantLock();

    public PartitionGroupStoreManager(String topic, int partitionGroup, File base, Config config,
                                      PreloadBufferPool bufferPool,
                                      ScheduledExecutorService scheduledExecutorService) {
        this(topic, partitionGroup, base, config,
                bufferPool,
                scheduledExecutorService, null);
    }


    public PartitionGroupStoreManager(String topic, int partitionGroup, File base, Config config,
                                      PreloadBufferPool bufferPool,
                                      ScheduledExecutorService scheduledExecutorService,
                                      VirtualThreadExecutor virtualThreadPool) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.base = base;
        this.topic = topic;
        this.partitionGroup = partitionGroup;
        this.config = config;
        this.writeCommandCache = new LinkedBlockingQueue<>(config.writeRequestCacheSize);
        this.bufferPool = bufferPool;
        this.started = new AtomicBoolean(false);
        this.enabled = new AtomicBoolean(false);
        StoreMessageSerializer storeMessageSerializer = new StoreMessageSerializer(config.maxMessageLength);
        this.store = new PositioningStore<>(base, config.storeConfig, bufferPool, storeMessageSerializer);
        if (!base.isDirectory()) {
            throw new StoreInitializeException(String.format("Partition group directory: %s not available!", base.getAbsolutePath()));
        }
        this.replicationPosition = store.flushPosition();
        term = getMaxTerm(store);

        flushCallbackBelt = new CallbackPositioningBelt();
        commitCallbackBelt = new CallbackPositioningBelt();
        this.virtualThreadPool = virtualThreadPool;
        this.writeLoopThread = LoopThread.builder()
                .name(String.format("WriteThread-%s-%d", topic, partitionGroup))
                .doWork(this::write)
                .sleepTime(0, 0)
                .onException(e -> logger.warn("Write Exception: ", e))
                .build();
        this.flushLoopThread = LoopThread.builder()
                .name(String.format("FlushThread-%s-%d", topic, partitionGroup))
                .doWork(this::flush)
                .sleepTime(config.flushIntervalMs, config.flushIntervalMs)
                .onException(e -> logger.warn("Flush Exception: ", e))
                .build();
        this.metricThread = initMetrics(config);
    }

    private LoopThread initMetrics(Config config) {
        if (config.printMetricIntervalMs > 0) {
            produceMetrics = new Metric("WriteMetric-" + topic + "-" + partitionGroup, 1,
                    new String[]{"WriteLatency", "FlushLatency"}, new String[]{"WriteCount", "FlushCount"},
                    new String[]{"WriteTraffic", "FlushTraffic"});
            produceMetric = produceMetrics.getMetricInstances().get(0);
            consumeMetrics = new Metric("ReadMetric-" + topic + "-" + partitionGroup, 1,
                    new String[]{"ReadLatency"}, new String[]{"ReadCount"},
                    new String[]{"ReadTraffic"});
            consumeMetric = consumeMetrics.getMetricInstances().get(0);

            return LoopThread.builder()
                    .sleepTime(config.printMetricIntervalMs, config.printMetricIntervalMs)
                    .name("Metric-Thread")
                    .onException(e -> logger.warn("Exception:", e))
                    .doWork(() -> {
                        consumeMetrics.reportAndReset();
                        produceMetrics.reportAndReset();
                        logger.info("{}-{} WriteCommandCache size: {}, dirty size: {}/{}.",
                                topic, partitionGroup, writeCommandCache.size(),
                                store.right() - store.flushPosition(), config.maxDirtySize);
                    }).build();
        } else {
            return null;
        }
    }

    public void recover() {
        try {
            logger.info("Recovering message store...");
            store.recover();
            logger.info("Recovering index store...");
            indexPosition = recoverPartitions();
            logger.info("Building indices ...");
            recoverIndices();
        } catch (Throwable e) {
            throw new StoreInitializeException(e);
        }
    }

    private void recoverIndices() throws IOException {

        // 删除indexPosition之后的所有索引
        for (Partition partition : partitionMap.values()) {
            partition.rollbackTo(indexPosition);
        }

        // 从indexPosition到store.right()重新构建索引
        while (indexPosition < store.right()) {
            ByteBuffer byteBuffer = store.read(indexPosition);
            if (null == byteBuffer)
                throw new ReadException(String.format("Read log failed! store: %s, position: %d.", store.base().getAbsolutePath(), indexPosition));
            IndexItem indexItem = IndexItem.parseMessage(byteBuffer, indexPosition);
            Partition partition = partitionMap.get(indexItem.getPartition());
            if(null == partition) {
                indexPosition += indexItem.getLength();
                continue;
            }
            PositioningStore<IndexItem> indexStore = partition.store;

            if (indexStore.right() == 0) {
                indexStore.setRight(indexItem.getIndex() * IndexItem.STORAGE_SIZE);
            }

            long storeIndex = indexStore.right() / IndexItem.STORAGE_SIZE;

            if (indexItem.getIndex() == storeIndex) {
                if (BatchMessageParser.isBatch(byteBuffer)) {
                    short batchSize = BatchMessageParser.getBatchSize(byteBuffer);
                    indexItem.setBatchMessage(true);
                    indexItem.setBatchMessageSize(batchSize);
                }
                writeIndex(indexItem, partition.store);

            } else if (indexItem.getIndex() < storeIndex) {
                IndexItem pi = indexStore.read(indexItem.getIndex() * IndexItem.STORAGE_SIZE);
                if (pi.getOffset() != indexPosition) {
                    throw new WriteException(
                            String.format(
                                    "Index mismatch, store: %s, partition: %d, next index of the partition: %s，index in log: %s, log position: %s, log: \n%s",
                                    this.base, indexItem.getPartition(),

                                    Format.formatWithComma(storeIndex),
                                    Format.formatWithComma(indexItem.getIndex()),
                                    Format.formatWithComma(indexPosition),
                                    MessageParser.getString(byteBuffer)));
                }
            } else if (indexItem.getIndex() > storeIndex) {
                throw new WriteException(
                        String.format(
                                "Index must be continuous, store: %s, partition: %d, next index of the partition: %s，index in log: %s, log position: %s, log: \n%s",
                                this.base, indexItem.getPartition(),

                                Format.formatWithComma(storeIndex),
                                Format.formatWithComma(indexItem.getIndex()),
                                Format.formatWithComma(indexPosition),
                                MessageParser.getString(byteBuffer)));
            }

            if (indexStore.right() - indexStore.flushPosition() >= 10 * 1024 * 1024) {
                indexStore.flush();
                logger.info("Write position: {}, index position: {}", store.right(), indexPosition);
            }
        }

        for (Partition partition : partitionMap.values()) {
            PositioningStore<IndexItem> indexStore = partition.store;
            if (indexStore.right() > indexStore.flushPosition()) {
                indexStore.flush();
            }
        }
    }

    private void rollbackPartitions(long messagePosition) throws IOException {
        for (Partition partition : partitionMap.values()) {
            partition.rollbackTo(messagePosition);
        }
    }

    private int getMaxTerm(PositioningStore<ByteBuffer> store) {
        int maxTerm = 0;
        if (store.right() > store.left()) {
            maxTerm = getEntryTerm(store.toLogStart(store.right()));
        }
        return maxTerm;
    }

    private long recoverPartitions() throws IOException {
        File indexBase = new File(base, "index");
        long indexPosition = store.right();
        // 恢复分区索引

        if (!indexBase.isDirectory()) {
            throw new StoreInitializeException(String.format("Index directory: %s not found! ", indexBase.getAbsolutePath()));
        }

        Short[] partitionIndices = loadPartitionIndices(indexBase);

        if (partitionIndices == null) return indexPosition;

        for (short partitionIndex : partitionIndices) {

            // 1. 创建Partition对象，加入到partitionMap中

            File partitionBase = new File(indexBase, String.valueOf(partitionIndex));
            PositioningStore<IndexItem> indexStore =
                    new PositioningStore<>(partitionBase, config.indexStoreConfig, bufferPool, new IndexSerializer());
            indexStore.recover();

            //截掉末尾可能存在的半条索引
            indexStore.setRight(indexStore.right() - indexStore.right() % IndexItem.STORAGE_SIZE);

            // 删除末尾的全是0的部分
            long validPosition = indexStore.right();
            IndexItem currentIndex, previousIndex = null;

            while ((validPosition -= IndexItem.STORAGE_SIZE) >= indexStore.left() + IndexItem.STORAGE_SIZE) { //第一条索引有可能是全0，这是合法的。
                if(null == previousIndex) {
                    currentIndex = indexStore.read(validPosition);
                } else {
                    currentIndex = previousIndex;
                }

                previousIndex = indexStore.read(validPosition - IndexItem.STORAGE_SIZE);
                if(verifyCurrentIndex(currentIndex, previousIndex)){
                    break;
                }
            }

            indexStore.setRight(validPosition + IndexItem.STORAGE_SIZE);


            partitionMap.put(partitionIndex, new Partition(indexStore));

            if (indexStore.right() > 0) {


                // 2. 如果最后一条索引是批消息的索引，需要检查其完整性

                IndexItem lastIndexItem = indexStore.read(indexStore.right() - IndexItem.STORAGE_SIZE);
                if (lastIndexItem == null) throw new ReadException(
                        String.format("Failed to recover index store %s to position %s, batchRead index failed!",
                                indexStore.base().getAbsolutePath(),
                                Format.formatWithComma(
                                        indexStore.right() - IndexItem.STORAGE_SIZE)));
                // 检查对应的消息是否批消息，如果是批消息检查这一批消息的索引的完整性，如不完整直接截掉这个批消息的已存储的所有索引
                lastIndexItem = verifyBatchMessage(lastIndexItem, indexStore, store);

                // 如果indexPosition大于当前分区索引的最大消息位置， 向前移动indexPosition
                long indexedMessagePosition = lastIndexItem.getOffset() + lastIndexItem.getLength();

                logger.info("Topic: {}, group: {}, partition: {}, maxIndexedMessageOffset: {}.", topic,
                        partitionGroup, partitionIndex, Format.formatWithComma(indexedMessagePosition));

                if (indexPosition > indexedMessagePosition) {
                    logger.info("Topic: {}, group: {}, set indexPosition from {} to {}.",
                            topic, partitionGroup, Format.formatWithComma(indexPosition),
                            Format.formatWithComma(indexedMessagePosition));
                    indexPosition = indexedMessagePosition;
                }
            }
        }

        return indexPosition;
    }

    /**
     * 根据上一条索引来验证这条索引的合法性
     * @param current 当前索引
     * @param previous 上一条索引
     * @return
     */
    private boolean verifyCurrentIndex(IndexItem current, IndexItem previous) {
        return current.getLength() > 0 && current.getOffset() > previous.getOffset();
    }

    private boolean isAllZero(IndexItem indexItem) {
        return indexItem.getLength() == 0 && indexItem.getOffset() == 0;
    }

    private Short[] loadPartitionIndices(File indexBase) {
        Short[] partitionIndices = null;
        File[] files = indexBase.listFiles(file -> file.isDirectory() && file.getName().matches("^\\d+$"));
        if (null != files) {
            partitionIndices = Arrays.stream(files)
                    .map(File::getName)
                    .map(str -> {
                        try {
                            return Short.parseShort(str);
                        } catch (NumberFormatException ignored) {
                            return (short) -1;
                        }
                    })
                    .filter(s -> s >= 0)
                    .toArray(Short[]::new);
        }
        return partitionIndices;
    }

    private IndexItem verifyBatchMessage(IndexItem lastIndexItem, PositioningStore<IndexItem> indexStore, PositioningStore<ByteBuffer> store) throws IOException {

        if (lastIndexItem.getOffset() < store.right()) {
            ByteBuffer msg = store.read(lastIndexItem.getOffset());
            if (BatchMessageParser.isBatch(msg)) {
                short batchSize = BatchMessageParser.getBatchSize(msg);
                long startIndex = MessageParser.getLong(msg, MessageParser.INDEX);

                if (indexStore.right() < (batchSize + startIndex) * IndexItem.STORAGE_SIZE) {
                    logger.info("Incomplete batch message indices found, roll back index store to {}.",
                            Format.formatWithComma(startIndex * IndexItem.STORAGE_SIZE));
                    indexStore.setRight(startIndex * IndexItem.STORAGE_SIZE);
                    lastIndexItem = indexStore.read(indexStore.right() - IndexItem.STORAGE_SIZE);
                }
            }
        }
        return lastIndexItem;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    Short[] listPartitions() {
        return partitionMap.keySet().toArray(new Short[0]);
    }

    private void removePartition(short partition) {
        Partition p = partitionMap.remove(partition);
        if (null != p) {
            // 重命名目录
            File partitionBase = new File(base, "index" + File.separator + partition);
            if (!partitionBase.renameTo(new File(partitionBase.getParent(), partitionBase.getName() + ".d." + SystemClock.now()))) {
                logger.warn("Rename directory {} failed!", partitionBase.getAbsolutePath());
            }
        }

    }

    private void addPartition(short partition) throws IOException {
        if (partitionMap.get(partition) == null) {
            // 如果存在分区目录，先删除
            removePartition(partition);

            File partitionBase = new File(base, "index" + File.separator + partition);
            if (partitionBase.mkdirs()) {
                PositioningStore<IndexItem> indexStore =
                        new PositioningStore<>(partitionBase, config.indexStoreConfig, bufferPool, new IndexSerializer());
                indexStore.recover();
                partitionMap.put(partition, new Partition(indexStore));
            } else {
                throw new IOException(String.format("Create directory: %s failed!", partitionBase.getAbsolutePath()));
            }
        }
    }

    public ReadResult read(short partition, long index, int count, long maxSize) throws IOException {
        long t0 = System.nanoTime();
        ReadResult readResult = new ReadResult();
        checkPartition(partition);
        PositioningStore<IndexItem> indexStore = partitionMap.get(partition).store;
        List<IndexItem> indexItemList = indexStore.batchRead(index * IndexItem.STORAGE_SIZE, count);

        long size = 0L;
        readResult.setEop(indexItemList.size() < count);
        List<ByteBuffer> buffers = new ArrayList<>(count);
        IndexItem lastIndexItem = null;
        for (int i = 0; i < indexItemList.size(); i++) {
            IndexItem indexItem = indexItemList.get(i);
            // 如果索引的offset和上一条相同，说明它们是同一批消息，直接跳过即可
            if (null != lastIndexItem && indexItem.getOffset() == lastIndexItem.getOffset()) {
                continue;
            }
            try {
                //FIXME: 偶尔会发生索引长度错误导致读消息抛异常，
                // 临时解决方案是捕获异常后，再用不传长度的方法试一次。
                // 另，用带长度的方法读性能更好。
                ByteBuffer log;
                try {
                    log = store.read(indexItem.getOffset(), indexItem.getLength());
                    if (MessageParser.getInt(log, MessageParser.LENGTH) != indexItem.getLength()) {
                        logger.warn("索引中消息长度不正确！index: {} , offset: {}, message length (from index/from message): {}/{}, partition: {}, store: {}.",
                                Format.formatWithComma(index + i),
                                Format.formatWithComma(indexItem.getOffset()),
                                indexItem.getLength(), MessageParser.getInt(log, MessageParser.LENGTH),
                                partition,
                                base.getAbsolutePath());
                        log = store.read(indexItem.getOffset());
                    }
                } catch (Throwable t) {
                    logger.warn("Exception on read, try to read without length! index: {} , offset: {}, message length: {}, partition: {}, store: {}.",
                            Format.formatWithComma(index + i),
                            Format.formatWithComma(indexItem.getOffset()),
                            indexItem.getLength(),
                            partition,
                            base.getAbsolutePath(),
                            t);
                    log = store.read(indexItem.getOffset());
                }

                if (null != log) {
                    if (maxSize <= 0 || (size += log.remaining()) < maxSize) {
                        buffers.add(log);
                    } else {
                        break;
                    }
                } else {
                    throw new ReadException(String.format("Read log failed! store: %s, position: %d.", store.base().getAbsolutePath(), indexItem.getOffset()));
                }
                lastIndexItem = indexItem;

            } catch (Throwable t) {
                logger.warn("Exception on read! index: {} , offset: {}, message length: {}, partition: {}, store: {}.",
                        Format.formatWithComma(index + i),
                        Format.formatWithComma(indexItem.getOffset()),
                        indexItem.getLength(),
                        partition,
                        base.getAbsolutePath(),
                        t);
                throw t;
            }
        }
        readResult.setMessages(buffers.toArray(new ByteBuffer[0]));
        readResult.setCode(JoyQueueCode.SUCCESS);
        if (null != consumeMetric) {
            consumeMetric.addCounter("ReadCount", buffers.size());
            consumeMetric.addLatency("ReadLatency", System.nanoTime() - t0);
            consumeMetric.addTraffic("ReadTraffic", buffers.stream().mapToInt(ByteBuffer::remaining).sum());
        }

        return readResult;
    }

    private void checkPartition(short partition) {
        if (!partitionMap.containsKey(partition))
            throw new ReadException(String.format("No such partition: %d in topic: %s, partition group: %d.", partition, topic, partitionGroup));
    }

    private long[] write(ByteBuffer... byteBuffers) throws IOException {
        long start = store.right();
        long position = start;
        long[] indices = new long[byteBuffers.length];
        try {
            for (int i = 0, byteBuffersLength = byteBuffers.length; i < byteBuffersLength; i++) {
                ByteBuffer byteBuffer = byteBuffers[i].slice();
                if (byteBuffer.remaining() > config.maxMessageLength) {
                    throw new WriteException(String.format("Message too large! Message length: %d, limit: %d", byteBuffer.remaining(), config.maxMessageLength));
                }
                // 生成索引
                IndexItem indexItem = IndexItem.parseMessage(byteBuffer, position);
                Partition partition = partitionMap.get(indexItem.getPartition());
                indices[i] = partition.store.right() / IndexItem.STORAGE_SIZE;

                MessageParser.setLong(byteBuffer, MessageParser.INDEX, indices[i]);
                indexItem.setIndex(indices[i]);

                int l = MessageParser.getInt(byteBuffer, MessageParser.LENGTH);
                long p = position;
                // 写入消息
                position = store.append(byteBuffer);
                // 写入索引

                if (BatchMessageParser.isBatch(byteBuffer)) {
                    short batchSize = BatchMessageParser.getBatchSize(byteBuffer);
                    indexItem.setBatchMessage(true);
                    indexItem.setBatchMessageSize(batchSize);
                }
                //TODO: 临时增加检查索引长度不正确问题
                if (indexItem.getLength() != l) {
                    logger.warn("检测到写入索引长度不正确：" +
                                    "indexItem.length: {}, length from message: {}, position: {}, topic={}, " +
                                    "partitionGroup={}, partition={}, index={}.",
                            indexItem.getLength(), l, p, topic, partitionGroup,
                            indexItem.getPartition(), indexItem.getIndex());
                }
                writeIndex(indexItem, partition.store);
                flushLoopThread.wakeup();
            }
        } catch (Throwable t) {
            logger.warn("Write failed, rollback to position: {}, topic={}, partitionGroup={}.", start, topic, partitionGroup, t);
            try {
                setRightPosition(start);
            } catch (Throwable e) {
                logger.warn("Rollback failed, rollback to position: {}, topic={}, partitionGroup={}.", start, topic, partitionGroup, e);
            }
            throw t;
        }
        return indices;
    }

    private void writeIndex(IndexItem indexItem, PositioningStore<IndexItem> indexStore) throws IOException {
        if (indexItem.isBatchMessage()) {
            // 批消息内每条消息的索引都指向批消息的起始位置，长度都是都批消息的总长度
            appendBatchMessageIndices(indexStore, indexItem);
        } else {
            indexStore.append(indexItem);
        }
        indexPosition += indexItem.getLength();
    }

    private void appendBatchMessageIndices(PositioningStore<IndexItem> indexStore, IndexItem indexItem) throws IOException {
        ByteBuffer indexBuffer = ByteBuffer.allocate(indexItem.getBatchMessageSize() * IndexItem.STORAGE_SIZE);
        for (int j = 0; j < indexItem.getBatchMessageSize(); j++) {
            indexItem.serializeTo(indexBuffer);
        }
        indexBuffer.flip();
        indexStore.appendByteBuffer(indexBuffer);
    }

    @Deprecated
    private boolean writeVT() {
        boolean ret = false;
        WriteCommand writeCommand;
        if (null != (writeCommand = writeCommandCache.poll())) {
            try {
                if (waitForFlush()) {
                    writeCommand.eventListener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_TIMEOUT, null));
                } else {
                    long[] indices = write(writeCommand.messages);
                    handleCallback(writeCommand, store.right(), indices);
                }
                ret = true;
            } catch (Throwable t) {
                if (writeCommand.eventListener != null)
                    writeCommand.eventListener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_FAILED, null));
            }
        }
        return ret;
    }

    private void write() throws IOException, InterruptedException {
        WriteCommand writeCommand = null;
        if(!writeLock.tryLock()) {
            throw new IllegalStateException("Acquire write lock failed!");
        }
        try {
            writeCommand = writeCommandCache.take();
            if (null != produceMetric) {
                produceMetric.addTraffic("WriteTraffic", Arrays.stream(writeCommand.messages).mapToInt(ByteBuffer::remaining).sum());
            }
            long t0 = System.nanoTime();

            verifyState(true);

            if (waitForFlush()) {
                writeCommand.eventListener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_TIMEOUT, null));
            } else {
                long[] indices = write(writeCommand.messages);
                handleCallback(writeCommand, store.right(), indices);
            }
            long t1 = System.nanoTime();
//
            if (null != produceMetric) {
                produceMetric.addLatency("WriteLatency", t1 - t0);
                produceMetric.addCounter("WriteCount", 1);

            }
        } catch (DiskFullException e) {
            if (null != writeCommand && writeCommand.eventListener != null)
                writeCommand.eventListener.onEvent(new WriteResult(JoyQueueCode.SE_DISK_FULL, null));
            throw e;
        } catch (IllegalStateException e) {
            if (null != writeCommand && writeCommand.eventListener != null)
                writeCommand.eventListener.onEvent(new WriteResult(JoyQueueCode.CY_STATUS_ERROR, null));
            throw e;
        } catch (Throwable t) {
            if (null != writeCommand && writeCommand.eventListener != null)
                writeCommand.eventListener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_FAILED, null));
            throw t;
        } finally {
            writeLock.unlock();
        }
    }

    private void verifyState(boolean expectedState) {
        if(enabled.get() != expectedState) {
            throw new IllegalStateException();
        }
    }

    private boolean waitForFlush() {

        long t0 = SystemClock.now();
        while (store.right() - store.flushPosition() >= config.maxDirtySize && SystemClock.now() - t0 <= config.writeTimeoutMs) {
            Thread.yield();
        }
        return SystemClock.now() - t0 > config.writeTimeoutMs;
    }

    private void handleCallback(WriteCommand writeCommand, long position, long[] indices) {
        Callback callback = new Callback(writeCommand.qosLevel, writeCommand.eventListener, indices);
        callback.position = position;

        // 处理回调
        switch (writeCommand.qosLevel) {
            case PERSISTENCE:
                flushCallbackBelt.put(callback);
                break;
            case REPLICATION:
                commitCallbackBelt.put(callback);
                break;
        }
    }

    private boolean callbackVT() {
        boolean ret = false;
        try {
            if (flushCallbackBelt.getFirst().position <= flushPosition()) {
                flushCallbackBelt.callbackBefore(flushPosition());
                ret = true;
            }
        } catch (NoSuchElementException ignored) {
        }
        return ret;
    }

    private void flush() {
        try {
            boolean flushed;
            do {
                long t0 = System.nanoTime();
                long before = store.flushPosition();
                flushed = store.flush() | flushIndices();

                if (null != produceMetric && flushed) {
                    long t1 = System.nanoTime();
                    produceMetric.addTraffic("FlushTraffic", store.flushPosition() - before);
                    produceMetric.addLatency("FlushLatency", t1 - t0);
                    produceMetric.addCounter("FlushCount", 1);
                }
            } while (flushed);
        } catch (IOException e) {
            logger.warn("Exception:", e);
        }
    }

    private boolean flushIndices() {
        boolean ret = false;

        try {
            boolean flushed;
            do {
                flushed = false;
                for (Partition partition : partitionMap.values()) {
                    flushed = partition.store.flush() || flushed;
                }
                ret = ret || flushed;
            } while (flushed);
        } catch (Exception e) {
            logger.warn("Exception: ", e);
        }
        return ret;
    }

    void asyncWrite(QosLevel qosLevel, EventListener<WriteResult> eventListener, WriteRequest... writeRequests) {

        if (!enabled.get())
            throw new WriteException(String.format("Store disabled! topic: %s, partitionGroup: %d.", topic, partitionGroup));
        ByteBuffer[] messages = new ByteBuffer[writeRequests.length];
        for (int i = 0, writeRequestsLength = writeRequests.length; i < writeRequestsLength; i++) {
            WriteRequest writeRequest = writeRequests[i];

            ByteBuffer byteBuffer = writeRequest.getBuffer();
            int length;
            if ((length = MessageParser.getInt(byteBuffer, MessageParser.LENGTH)) != byteBuffer.remaining()) {
                throw new WriteException(String.format("Message length check error! Expect: %d, actual: %d", length, byteBuffer.remaining()));
            }

            if (!partitionMap.containsKey(writeRequest.getPartition())) {
                throw new WriteException(String.format("No partition %d in partition group %d of topic %s!", writeRequest.getPartition(), partitionGroup, topic));
            }

            // 分区
            MessageParser.setShort(byteBuffer, MessageParser.PARTITION, writeRequest.getPartition());
            // 轮次
            MessageParser.setInt(byteBuffer, MessageParser.TERM, term);
            // 存储时间：与发送时间的差值
            MessageParser.setInt(byteBuffer, MessageParser.STORAGE_TIMESTAMP,
                    (int) (SystemClock.now() - MessageParser.getLong(byteBuffer, MessageParser.CLIENT_TIMESTAMP)));
            messages[i] = writeRequest.getBuffer();
        }
        WriteCommand writeCommand = new WriteCommand(qosLevel, eventListener, messages);
        try {
            this.writeCommandCache.put(writeCommand);
        } catch (InterruptedException e) {
            logger.warn("Exception: ", e);
            if (eventListener != null)
                eventListener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_FAILED, null));
        }

        if (qosLevel == QosLevel.RECEIVE && null != eventListener) {
            eventListener.onEvent(new WriteResult(JoyQueueCode.SUCCESS, null));
        }
    }

    long indexPosition() {
        return indexPosition;
    }

    PositioningStore<IndexItem> indexStore(short partition) {
        if (partitionMap.containsKey(partition)) {
            return partitionMap.get(partition).store;
        } else {
            return null;
        }
    }

    PositioningStore<ByteBuffer> messageStore() {
        return store;
    }

    Set<PositioningStore<IndexItem>> meetPositioningStores() {
        return partitionMap.values().stream().map(p -> p.store).collect(Collectors.toSet());
    }

    /**
     * 重新分区
     */
    synchronized void rePartition(Short[] partitions) throws IOException {


        for (short partition : partitions) {
            if (!partitionMap.containsKey(partition)) {
                addPartition(partition);
            }
        }

        List<Short> partitionList = Arrays.asList(partitions);
        List<Short> toBeRemoved = new ArrayList<>();
        for (Map.Entry<Short, Partition> entry : partitionMap.entrySet()) {
            if (!partitionList.contains(entry.getKey())) {
                toBeRemoved.add(entry.getKey());
            }
        }
        for (Short partition : toBeRemoved) {
            removePartition(partition);
        }
    }

    @Override
    public synchronized void start() {
        if (config.printMetricIntervalMs > 0) {
            metricThread.start();
        }
        startCallbackThread();
        startFlushThread();
        started.set(true);
    }


    private void startFlushThread() {
        flushLoopThread.start();
    }

    private void startCallbackThread() {
        if (null != virtualThreadPool) {
            this.virtualThreadPool.start(callbackVirtualThread, config.flushIntervalMs, String.format("CallbackThread-%s-%d", topic, partitionGroup));
        } else {
            callbackFeature = scheduledExecutorService.scheduleAtFixedRate(this::callbackVT,
                    ThreadLocalRandom.current().nextLong(500L, 1000L),
                    config.flushIntervalMs, TimeUnit.MILLISECONDS);
        }
    }

    private void startWriteThread() {
        if (null != virtualThreadPool) {
            this.virtualThreadPool.start(writeVirtualThread, String.format("WriteThread-%s-%d", topic, partitionGroup));
        } else {
            this.writeLoopThread.start();
        }
    }

    @Override
    public synchronized void stop() {
        // 此处不用logger打印异常的原因是：
        // 调用close方法一般是程序退出时，
        // 此时logger已经被关闭，无法使用。
        try {
            if (started.compareAndSet(true, false)) {
                long stopTimeout = 5000L;
                System.out.println("Waiting for flush finished...");
                long t0 = SystemClock.now();
                try {
                    while (SystemClock.now() - t0 < stopTimeout &&
                            !isAllStoreClean()) {
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                stopFlushThread();
                stopCallbackThread(stopTimeout);
                if (config.printMetricIntervalMs > 0) {
                    metricThread.stop();
                }
            }
        } catch (Throwable t) {
           logger.error(t.getMessage(),t);
        }
    }

    private boolean isAllStoreClean() {
        return Stream.concat(Stream.of(store), partitionMap.values().stream().map(partition -> partition.store)).allMatch(PositioningStore::isClean);
    }

    private void stopCallbackThread(long stopTimeout) throws TimeoutException {
        if (null != virtualThreadPool) {
            safeStop("Stopping callback thread...", callbackVirtualThread);
        } else {
            stopAndWaitScheduledFeature(callbackFeature, stopTimeout);
        }
    }

    private void stopFlushThread() {
        flushLoopThread.stop();
    }

    private void stopWriteThread() {
        if (null != virtualThreadPool) {
            safeStop("Stopping write thread...", writeVirtualThread);
        } else {
            writeLoopThread.stop();
        }
    }

    private void safeStop(String s, VirtualThread flushVirtualThread) {
        logger.info(s);
        System.out.println(s);
        try {
            this.virtualThreadPool.stop(flushVirtualThread);
        } catch (InterruptedException e) {
            logger.warn("Exception: ", e);
        }
    }

    private void stopAndWaitScheduledFeature(ScheduledFuture scheduledFuture, long timeout) throws TimeoutException {
        if (scheduledFuture != null) {
            long t0 = SystemClock.now();
            while (!scheduledFuture.isDone()) {
                if (SystemClock.now() - t0 > timeout) {
                    throw new TimeoutException("Wait for async job timeout!");
                }
                scheduledFuture.cancel(true);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    logger.warn("Exception: ", e);
                }
            }
        }
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    long getLeftIndex(short partition) {
        long index = -1;
        Partition p = partitionMap.get(partition);
        if (null != p) {
            index = p.store.left() / IndexItem.STORAGE_SIZE;
        }

        return index;
    }

    public long getRightIndex(short partition) {
        long index = -1;
        Partition p = partitionMap.get(partition);
        if (null != p) {
            index = p.store.right() / IndexItem.STORAGE_SIZE;
        }

        return index;
    }

    /**
     * 节点服务状态
     */
    @Override
    public boolean serviceStatus() {
        return enabled.get();
    }

    @Override
    public void enable() {
        if (!enabled.get()) {
            enabled.set(true);
            startWriteThread();
        }
    }

    @Override
    public void disable() {
        if (enabled.get()) {
            writeCommandCache.clear();
            stopWriteThread();
            enabled.set(false);
        }
    }

    @Override
    public void setRightPosition(long position) throws IOException {
        stopFlushThread();
        try {
            rollback(position);
        } finally {
            startFlushThread();
        }
    }

    @Override
    public void clear(long position) throws IOException {
        stopFlushThread();
        try {
            for (Partition partition : partitionMap.values()) {
                partition.store.setRight(0L);
            }
            store.clear(position);
        } finally {
            startFlushThread();
        }
    }




    private void rollback(long position) throws IOException {

        boolean clearIndexStore = position <= leftPosition() || position > rightPosition();

        // 如果store整个删除干净了，需要把index也删干净
        // FIXME: 考虑这种情况：FOLLOWER被rollback后，所有文件都被删除了，但它有一个非零的writePosition，index是0，
        //  如果被选为LEADER，index是不正确的。
        if (clearIndexStore) {
            for (Partition partition : partitionMap.values()) {
                partition.store.setRight(0L);
            }
        } else {
            rollbackPartitions(position);
        }

        store.setRight(position);
    }


    long flushPosition() {
        return store.flushPosition();
    }

    /**
     * 集群提交的位置 flushPosition <= commitPosition
     */
    @Override
    public long commitPosition() {
        return this.replicationPosition;
    }

    /**
     * 当前任期
     */
    @Override
    public int term() {
        return term;
    }

    /**
     * 切换下一任期：return ++term;
     */
    @Override
    public void term(int term) {
        this.term = term;
    }


    @Override
    public ByteBuffer readEntryBuffer(long position, int length) throws IOException {
        long t0 = System.nanoTime();
        ByteBuffer buffer = store.readByteBuffer(position, length);
        if (null != consumeMetric) {
            consumeMetric.addCounter("ReadCount", 1);
            consumeMetric.addLatency("ReadLatency", System.nanoTime() - t0);
            consumeMetric.addTraffic("ReadTraffic", buffer.remaining());
        }

        return buffer;
    }


    @Override
    public long appendEntryBuffer(ByteBuffer byteBuffer) throws IOException, TimeoutException {
        if(!writeLock.tryLock()) {
            throw new IllegalStateException("Acquire write lock failed!");
        }
        try {
            verifyState(false);
            long t0 = System.nanoTime();
            if (waitForFlush()) {
                throw new TimeoutException("Wait for flush timeout! The broker is too much busy to write data to disks.");
            }
            long start = store.right();
            int counter = 0;
            int size = byteBuffer.remaining();
            try {
                // 写入消息
                long position = store.appendByteBuffer(byteBuffer.asReadOnlyBuffer());

                // 写入索引
                while (byteBuffer.hasRemaining()) {
                    IndexItem indexItem = IndexItem.parseMessage(byteBuffer, start + byteBuffer.position());
                    Partition partition = partitionMap.get(indexItem.getPartition());
                    if (partition.store.right() == 0L) {
                        partition.store.setRight(indexItem.getIndex() * IndexItem.STORAGE_SIZE);
                    } else if (indexItem.getIndex() * IndexItem.STORAGE_SIZE != partition.store.right()) {
                        throw new WriteException(
                                String.format(
                                        "Index must be continuous, store: %s, partition: %d, next index of the partition: %s，index in log: %s, log position: %s, log: \n%s",
                                        this.base, indexItem.getPartition(),
                                        Format.formatWithComma(partition.store.right() / IndexItem.STORAGE_SIZE),
                                        Format.formatWithComma(indexItem.getIndex()),
                                        Format.formatWithComma(start + byteBuffer.position()),
                                        MessageParser.getString(byteBuffer)));
                    }

                    if (BatchMessageParser.isBatch(byteBuffer)) {
                        short batchSize = BatchMessageParser.getBatchSize(byteBuffer);
                        indexItem.setBatchMessage(true);
                        indexItem.setBatchMessageSize(batchSize);
                    }

                    writeIndex(indexItem, partition.store);
                    byteBuffer.position(byteBuffer.position() + indexItem.getLength());
                    counter++;
                }

                if (null != produceMetric) {
                    long t1 = System.nanoTime();
                    produceMetric.addTraffic("WriteTraffic", size);
                    produceMetric.addLatency("WriteLatency", t1 - t0);
                    produceMetric.addCounter("WriteCount", counter);

                }

                return position;
            } catch (Throwable t) {
                logger.warn("Write failed, rollback to position: {}, topic={}, partitionGroup={}.", start, topic, partitionGroup, t);
                try {
                    setRightPosition(start);
                } catch (Throwable e) {
                    logger.warn("Rollback failed, rollback to position: {}, topic={}, partitionGroup={}.", start, topic, partitionGroup, e);
                }
                throw t;
            }
        }finally {
            writeLock.unlock();
        }
    }

    /**
     * 计算日志相对于position的位置
     *
     * @param position    当前位置，必须是日志的起始位置
     * @param offsetCount 偏移的消息条数，可以为负数
     */
    @Override
    public long position(long position, int offsetCount) {
        return store.position(position, offsetCount);
    }


    /**
     * LEADER 收到半数以上回复后，调用此方法提交
     * FOLLOWER 收到LEADER 从
     */
    @Override
    public void commit(long position) {
        commitCallbackBelt.callbackBefore(position);
        if (position > replicationPosition) {
            replicationPosition = position;
        }


    }

    @Override
    public int getEntryTerm(long position) {
        int term = 0;
        if (store.right() > store.left()) {
            try {
                ByteBuffer log = store.read(position);
                if (log != null) {
                    int logTerm = MessageParser.getInt(log, MessageParser.TERM);
                    if (logTerm >= 0) {
                        term = logTerm;
                    }
                } else {
                    throw new ReadException(String.format("Read log failed! store: %s, position: %d.", store.base().getAbsolutePath(), position));
                }
            } catch (Exception e) {
                throw new ReadException(String.format("Read log failed! store: %s, position: %d.", store.base().getAbsolutePath(), position), e);
            }
        }
        return term;
    }

    @Override
    public long leftPosition() {
        return store.left();
    }

    public long rightPosition() {
        return store.right();
    }

    @Override
    public void close() {
        if (null != store) store.close();
        for (Partition partition : partitionMap.values()) {
            partition.store.close();
        }
    }

    /**
     * 根据消息存储时间获取索引。
     * 如果找到，返回最后一条 “存储时间 <= timestamp” 消息的索引。
     * 如果找不到，返回负值。
     */
    public long getIndex(short partition, long timestamp) {

        try {
            if (partitionMap.containsKey(partition)) {
                PositioningStore<IndexItem> indexStore = partitionMap.get(partition).store;
                long searchedIndex = binarySearchByTimestamp(timestamp, store, indexStore, indexStore.left() / IndexItem.STORAGE_SIZE, indexStore.right() / IndexItem.STORAGE_SIZE - 1);

                // 考虑到有可能出现连续n条消息时间相同，找到这n条消息的第一条
                while (searchedIndex - 1 >= indexStore.left() && timestamp <= getStorageTimestamp(store, indexStore, searchedIndex - 1)) {
                    searchedIndex--;
                }
                return searchedIndex;

            }
        } catch (PositionOverflowException | PositionUnderflowException | IOException e) {
            logger.warn("Exception: ", e);
        }
        return -1L;
    }

    private long getStorageTimestamp(PositioningStore<ByteBuffer> journalStore,
                                     PositioningStore<IndexItem> indexStore,
                                     long index) throws IOException {
        IndexItem indexItem = indexStore.read(index * IndexItem.STORAGE_SIZE);
        ByteBuffer journal = journalStore.read(indexItem.getOffset(), indexItem.getLength());

        return MessageParser.getLong(journal, MessageParser.CLIENT_TIMESTAMP) + MessageParser.getInt(journal, MessageParser.STORAGE_TIMESTAMP);
    }

    // 折半查找
    private long binarySearchByTimestamp(long timestamp,
                                         PositioningStore<ByteBuffer> journalStore,
                                         PositioningStore<IndexItem> indexStore,
                                         long leftIndexInclude,
                                         long rightIndexInclude) throws IOException {

        if (rightIndexInclude <= leftIndexInclude) {
            return -1L;
        }

        if (timestamp <= getStorageTimestamp(journalStore, indexStore, leftIndexInclude)) {
            return leftIndexInclude;
        }

        if (timestamp > getStorageTimestamp(journalStore, indexStore, rightIndexInclude)) {
            return -1;
        }

        if (leftIndexInclude + 1 == rightIndexInclude) {
            return rightIndexInclude;
        }

        long mid = leftIndexInclude + (rightIndexInclude - leftIndexInclude) / 2;

        long midTimestamp = getStorageTimestamp(journalStore, indexStore, mid);

        if (timestamp < midTimestamp) {
            return binarySearchByTimestamp(timestamp, journalStore, indexStore, leftIndexInclude, mid);
        } else {
            return binarySearchByTimestamp(timestamp, journalStore, indexStore, mid, rightIndexInclude);
        }
    }

    QosStore getQosStore(QosLevel level) {
        return qosStores[level.value()];
    }

    private static class Callback {
        long position;
        EventListener<WriteResult> listener;
        long[] indices;
        long timestamp;
        QosLevel qosLevel;
        Callback(QosLevel qosLevel, EventListener<WriteResult> listener, long[] indices) {
            this.listener = listener;
            this.indices = indices;
            this.qosLevel = qosLevel;
            this.timestamp = SystemClock.now();
        }
    }

    private static class Partition {
        private final PositioningStore<IndexItem> store;


        private Partition(PositioningStore<IndexItem> store) {
            this.store = store;
        }

        private void rollbackTo(long messagePosition) throws IOException {

            long indexPosition = store.right() - IndexItem.STORAGE_SIZE;
            while (indexPosition >= store.left()) {
                IndexItem indexItem = store.read(indexPosition);
                if (null != indexItem) {
                    if (indexItem.getOffset() + indexItem.getLength() <= messagePosition) break;
                } else {
                    throw new RollBackException(String.format("Failed to rollback store %s to position %d, batchRead index failed!", store.base().getAbsolutePath(), messagePosition));
                }
                indexPosition -= IndexItem.STORAGE_SIZE;
            }

            store.setRight(indexPosition <= store.left() ? 0L : indexPosition + IndexItem.STORAGE_SIZE);

        }

    }

    private static class WriteCommand {
        private final QosLevel qosLevel;
        private final EventListener<WriteResult> eventListener;
        private final ByteBuffer[] messages;

        private WriteCommand(QosLevel qosLevel, EventListener<WriteResult> eventListener, ByteBuffer[] messages) {
            this.qosLevel = qosLevel;
            this.eventListener = eventListener;
            this.messages = messages;
        }
    }

    public static class Config {
        public static final int DEFAULT_MAX_MESSAGE_LENGTH = 4 * 1024 * 1024;
        public static final int DEFAULT_WRITE_REQUEST_CACHE_SIZE = 1024;
        public static final long DEFAULT_FLUSH_INTERVAL_MS = 20L;
        public static final long DEFAULT_WRITE_TIMEOUT_MS = 3000L;
        public static final long DEFAULT_MAX_DIRTY_SIZE = 10L * 1024 * 1024;
        public static final long DEFAULT_PRINT_METRIC_INTERVAL_MS = 0L;

        /**
         * 允许脏数据的最大长度，超过这个长度就阻塞写入。
         * 目的是防止刷盘慢于写入速度时，内存中的脏数据越来越多导致OOM
         */
        private final long maxDirtySize;

        /**
         * 写入超时时间
         */
        private final long writeTimeoutMs;
        /**
         * 最大消息长度
         */
        private final int maxMessageLength;

        /**
         * 写入请求缓存的大小
         */
        private final int writeRequestCacheSize;

        /**
         * 异步刷盘的时间间隔(ms)
         */
        private final long flushIntervalMs;

        /**
         * 打印性能信息的时间间隔
         */
        private final long printMetricIntervalMs;

        private PositioningStore.Config storeConfig;
        private PositioningStore.Config indexStoreConfig;

        public Config() {

            this(DEFAULT_MAX_MESSAGE_LENGTH, DEFAULT_WRITE_REQUEST_CACHE_SIZE, DEFAULT_FLUSH_INTERVAL_MS,
                    DEFAULT_WRITE_TIMEOUT_MS, DEFAULT_MAX_DIRTY_SIZE, DEFAULT_PRINT_METRIC_INTERVAL_MS,
                    new PositioningStore.Config(PositioningStore.Config.DEFAULT_FILE_DATA_SIZE),
                    new PositioningStore.Config(PositioningStore.Config.DEFAULT_FILE_DATA_SIZE));
        }

        public Config(int maxMessageLength, int writeRequestCacheSize, long flushIntervalMs,
                      long writeTimeoutMs, long maxDirtySize, long printMetricIntervalMs,
                      PositioningStore.Config storeConfig, PositioningStore.Config indexStoreConfig) {
            this.maxMessageLength = maxMessageLength;
            this.writeRequestCacheSize = writeRequestCacheSize;
            this.flushIntervalMs = flushIntervalMs;
            this.writeTimeoutMs = writeTimeoutMs;
            this.maxDirtySize = maxDirtySize;
            this.printMetricIntervalMs = printMetricIntervalMs;
            this.storeConfig = storeConfig;
            this.indexStoreConfig = indexStoreConfig;
        }
    }

    class CallbackPositioningBelt {
        private final ConcurrentLinkedQueue<Callback> queue = new ConcurrentLinkedQueue<>();
        private AtomicLong callbackPosition = new AtomicLong(0L);

        Callback getFirst() {
            final Callback f = queue.peek();
            if (f == null)
                throw new NoSuchElementException();
            return f;
        }

        Callback removeFirst() {
            final Callback f = queue.poll();
            if (f == null)
                throw new NoSuchElementException();
            return f;
        }

        boolean remove(Callback callback) {
            return queue.remove(callback);
        }

        void addLast(Callback callback) {
            queue.add(callback);
        }

        /**
         * NOT Thread-safe!!!!!!
         */
        void callbackBefore(long position) {
            callbackPosition.set(position);
            try {
                while (getFirst().position <= position) {
                    Callback callback = removeFirst();
                    callback.listener.onEvent(new WriteResult(JoyQueueCode.SUCCESS, callback.indices));
                }
                long deadline = SystemClock.now() - EVENT_TIMEOUT_MILLS;
                while (getFirst().timestamp < deadline) {
                    Callback callback = removeFirst();
                    callback.listener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_TIMEOUT, null));
                }
            } catch (NoSuchElementException ignored) {
            }
        }

        void put(Callback callback) {
            addLast(callback);
            if (callback.position <= callbackPosition.get() && remove(callback)) {
                callback.listener.onEvent(new WriteResult(JoyQueueCode.SUCCESS, callback.indices));
            }
        }
    }

}
