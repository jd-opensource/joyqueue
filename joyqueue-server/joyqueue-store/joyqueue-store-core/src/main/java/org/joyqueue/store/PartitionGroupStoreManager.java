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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.store.file.Checkpoint;
import org.joyqueue.store.file.DiskFullException;
import org.joyqueue.store.file.PositioningStore;
import org.joyqueue.store.file.RollBackException;
import org.joyqueue.store.file.StoreMessageSerializer;
import org.joyqueue.store.index.IndexItem;
import org.joyqueue.store.index.IndexSerializer;
import org.joyqueue.store.message.BatchMessageParser;
import org.joyqueue.store.message.MessageParser;
import org.joyqueue.store.replication.ReplicableStore;
import org.joyqueue.store.utils.PreloadBufferPool;
import org.joyqueue.toolkit.concurrent.CasLock;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.concurrent.LoopThread;
import org.joyqueue.toolkit.format.Format;
import org.joyqueue.toolkit.metric.Metric;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * root                 Partition group root
 * ├── 0                Message files
 * ├── checkpoint.json  Checkpoint file
 * └── index            Index directory
 *     ├── 4            Partition 4 directory
 *     │   └── 0        Index files of partition 4
 *     ├── 5            Partition 5 directory
 *     └── 6            Partition 6 directory
 * @author liyue25
 * Date: 2018/8/13
 */
public class PartitionGroupStoreManager extends Service implements ReplicableStore, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PartitionGroupStoreManager.class);
    // 回调超时
    private static final long EVENT_TIMEOUT_MILLS = 60 * 1000L;
    // Journal store
    private final PositioningStore<ByteBuffer> store;
    // Partition Group所在的目录
    private final File base;
    // 主题
    private final String topic;
    // 分组
    private final int partitionGroup;
    // 存放异步回调队列的Map，只有如下三种级别才进入这个回调Map
    //     PERSISTENCE：Broker将消息写入磁盘后应答；
    //     REPLICATION：Broker将消息复制到集群大多数节点后应答，默认值；
    //     ALL：REPLICATION and PERSISTENCE；
    private final Map<QosLevel, CallbackPositioningBelt> callbackMap = new HashMap<>(3);
    // 分区索引存储
    private final Map<Short /* Partition */, Partition> partitionMap = new ConcurrentHashMap<>();
    private final Config config;
    private final QosStore[] qosStores =
            {new QosStore(this, QosLevel.ONE_WAY),
                    new QosStore(this, QosLevel.RECEIVE),
                    new QosStore(this, QosLevel.PERSISTENCE),
                    new QosStore(this, QosLevel.REPLICATION),
                    new QosStore(this, QosLevel.ALL)
            };
    private final PreloadBufferPool bufferPool;
    // 写入线程，刷盘线程，每个Partition Group唯一
    private final LoopThread writeLoopThread, flushLoopThread;
    // 记录Metric的线程
    private final LoopThread metricThread;
    // 写入消息缓冲队列
    private final BlockingQueue<WriteCommand> writeCommandCache;
    // 复制位置，也就是提交位置。指向Journal store的绝对偏移量。
    private long replicationPosition;
    // 索引位置，该位置之前的消息都有索引。指向Journal store的绝对偏移量。
    private long indexPosition;
    // Store是否可供生产消费。
    //  true：生产消费可以读写，复制线程不可读写；
    //  false：复制线程可读写，生产消费不可读写。
    private final AtomicBoolean enabled;
    private int term; // 当前任期，用于选举
    private Metric produceMetrics = null, consumeMetrics = null;
    private Metric.MetricInstance produceMetric = null, consumeMetric;

    // 数据写入锁，向store写入数据的时候持有。
    private final Lock writeLock = new ReentrantLock();

    // 记录上一次检查磁盘空间的时间戳，避免频繁去检查。
    private final AtomicLong lastCheckDiskSpaceTimestamp = new AtomicLong(0L);
    private volatile boolean isDiskFull = false; // 磁盘是否满了？
    private static final long CHECK_DISK_SPACE_COOL_DOWN = 1000L; // 最频繁每1秒检查一次磁盘空间。
    private static final long FLUSH_CHECKPOINT_INTERVAL_MS = 60 * 1000L; // 最频繁每1分钟更新一次CheckPoint
    private long lastFlushCheckpointTimestamp = 0L; // 上一次更新CheckPoint文件的时间。
    static final String CHECKPOINT_FILE= "checkpoint.json";
    private int lastEntryTerm = -1; // 最新一条消息的term
    private final CasLock flushLock = new CasLock(); // 刷盘锁，刷盘、回滚的时候需要持有这个锁。
    private final ReadWriteLock rollbackLock = new ReentrantReadWriteLock();

    public PartitionGroupStoreManager(String topic, int partitionGroup, File base, Config config,
                                      PreloadBufferPool bufferPool) {
        this.base = base;
        this.topic = topic;
        this.partitionGroup = partitionGroup;
        this.config = config;
        this.writeCommandCache = new LinkedBlockingQueue<>(config.writeRequestCacheSize);
        this.bufferPool = bufferPool;
        this.enabled = new AtomicBoolean(false);
        this.callbackMap.put(QosLevel.PERSISTENCE, new CallbackPositioningBelt());
        this.callbackMap.put(QosLevel.REPLICATION, new CallbackPositioningBelt());
        this.callbackMap.put(QosLevel.ALL, new CallbackPositioningBelt());
        StoreMessageSerializer storeMessageSerializer = new StoreMessageSerializer(config.maxMessageLength);
        this.store = new PositioningStore<>(base, config.storeConfig, bufferPool, storeMessageSerializer);
        if (!base.isDirectory()) {
            throw new StoreInitializeException(String.format("Partition group directory: %s not available!", base.getAbsolutePath()));
        }
        term = getMaxTerm(store);

        this.writeLoopThread = LoopThread.builder()
                .name(String.format("WriteThread-%s-%d", topic, partitionGroup))
                .doWork(this::write)
                .sleepTime(0, 0)
                .onException(e -> {
                    logger.warn("Write Exception: ", e);
                    try {
                      Thread.sleep(10);
                    } catch (InterruptedException ignored) {}
                })
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

    /**
     * 系统启动时，恢复存储信息。
     */
    public void recover() {
        try {
            // 恢复 Journal
            logger.info("Recovering message store {}...", base.getAbsolutePath());
            store.recover();

            // 恢复检查点文件，如果检查点文件损坏或者不存在，返回null，不影响继续恢复存储。
            logger.info("Recovering Checkpoint {}...", base.getAbsolutePath());
            Checkpoint checkpoint = recoverCheckpoint();

            // 恢复提交位置
            recoverReplicationPosition(checkpoint);

            // 恢复当前任期
            resetLastEntryTerm();

            // 恢复分区索引，并从索引文件中计算indexPosition
            logger.info("Recovering index store {}...", base.getAbsolutePath());
            indexPosition = recoverPartitions();
            long safeIndexPosition = indexPosition;

            // 从CheckPoint文件中恢复indexPosition，与之前的indexPosition比较，使用二者中较大的那个位置作为重建索引的起始位置。
            logger.info("Recovering index position from checkpoint {}...", base.getAbsolutePath());
            indexPosition = recoverIndexPositionFromCheckpoint(checkpoint);

            long checkPointIndexPosition = indexPosition;
            logger.info("Building indices {}...", base.getAbsolutePath());

            // 为了提高恢复速度，优先使用CheckPoint中记录的索引位置恢复。
            // 如果出现异常，再尝试从safeIndexPosition恢复。
            try {
                recoverIndices();
            } catch (Throwable t) {
                if (safeIndexPosition != indexPosition) {
                    indexPosition = safeIndexPosition;
                    logger.warn("Exception while recover indices using indexPosition {} from Checkpoint.json. " +
                                    "Fall back safe index position {} and retry recover indices...",
                            Format.formatWithComma(checkPointIndexPosition),
                            Format.formatWithComma(safeIndexPosition), t);
                    recoverIndices();
                } else {
                    logger.error("recover exception {}", base.getAbsolutePath(), t);
                    throw t;
                }
            }
            logger.info("Store recovered: {}...", base.getAbsolutePath());
        } catch (IOException e) {
            logger.error("recover exception {}", base.getAbsolutePath(), e);
            throw new StoreInitializeException(e);
        } catch (Exception e) {
            logger.error("recover exception {}", base.getAbsolutePath(), e);
            throw e;
        }
    }

    private void resetLastEntryTerm() {
        if(store.right() > 0) {
            long lastLogPosition = store.toLogStart(store.right());
            lastEntryTerm = getEntryTerm(lastLogPosition);
        } else {
            lastEntryTerm = -1;
        }
        logger.info("Reset last entry term to {}, store: {}.", lastEntryTerm, base.getAbsolutePath());
    }

    /**
     * 从indexPosition到store.right()重新构建索引
     * @throws IOException 出现IO异常时抛出
     */
    private void recoverIndices() throws IOException {

        while (indexPosition < store.right()) {
            ByteBuffer byteBuffer = store.read(indexPosition);
            if (null == byteBuffer)
                throw new ReadException(String.format("Read log failed! store: %s, position: %d.", store.base().getAbsolutePath(), indexPosition));
            IndexItem indexItem = IndexItem.parseMessage(byteBuffer, indexPosition);
            Partition partition = partitionMap.get(indexItem.getPartition());

            // 如果分区不存在，可能变更过分区，忽略这条消息继续
            if(null == partition) {
                indexPosition += indexItem.getLength();
                continue;
            }

            PositioningStore<IndexItem> indexStore = partition.store;
            long indexItemPosition = indexItem.getIndex() * IndexItem.STORAGE_SIZE;

            if (indexStore.right() == 0 || indexItemPosition < indexStore.right()) {
                // 如果当前分区没有索引，或者当前分区的下一条索引大于即将写入的索引，
                // 删除当前索引之后的所有索引。
                indexStore.setRight(indexItemPosition);
            } else if ( indexItemPosition > indexStore.right()) {
                // 正常情况下不会出现索引不连续
                throw new WriteException(
                        String.format(
                                "Index must be continuous, store: %s, partition: %d, next index of the partition: %s，index in log: %s, log position: %s, log: \n%s",
                                this.base, indexItem.getPartition(),

                                Format.formatWithComma(indexStore.right() / IndexItem.STORAGE_SIZE),
                                Format.formatWithComma(indexItem.getIndex()),
                                Format.formatWithComma(indexPosition),
                                MessageParser.getString(byteBuffer)));
            }

            // 写入索引
            if (BatchMessageParser.isBatch(byteBuffer)) {
                short batchSize = BatchMessageParser.getBatchSize(byteBuffer);
                indexItem.setBatchMessage(true);
                indexItem.setBatchMessageSize(batchSize);
            }
            writeIndex(indexItem, partition.store);

            // 每写入10M索引，执行一次刷盘
            if (indexStore.right() - indexStore.flushPosition() >= 10 * 1024 * 1024) {
                indexStore.flush();
                logger.info("Recovering index, topic: {}, group: {}, Write position: {}, index position: {}",
                        topic, partitionGroup, store.right(), indexPosition);
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
//    恢复PartitionGroup时，PartitionGroupStoreManager#recoverIndices方法中，增加如下逻辑：
//
//    读取checkpoint文件，检查每个分区的下一条索引序号是否大于等于checkpoint中记录的索引序号；
//    如果是，使用checkpoint中记录的indexPosition继续恢复索引；
//    否则，使用目前的逻辑计算出的indexPosition继续恢复索引。
    private long recoverIndexPositionFromCheckpoint(Checkpoint checkpoint) {
        if (null != checkpoint && checkpoint.getIndexPosition() > indexPosition &&
                checkpoint.getPartitions().entrySet().stream()
                        .allMatch(entry -> {
                            short partition = entry.getKey();
                            long index = entry.getValue();
                            Partition p = partitionMap.get(partition);
                            return null != p && p.store.right() >= index * IndexItem.STORAGE_SIZE;


                        })) {
            logger.info("Using indexPosition: {} from the checkpoint file.", checkpoint.getIndexPosition());
            return checkpoint.getIndexPosition();
        }
        return indexPosition;
    }
    private Checkpoint recoverCheckpoint() {
        try {
            File checkpointFile =  new File(base, CHECKPOINT_FILE);
            if(checkpointFile.isFile()) {
                byte[] serializedData = new byte[(int) checkpointFile.length()];
                try (FileInputStream fis = new FileInputStream(checkpointFile)) {
                    if (serializedData.length != fis.read(serializedData)) {
                        throw new IOException("File length not match!");
                    }
                }
                String jsonString = new String(serializedData, StandardCharsets.UTF_8);
                Checkpoint checkpoint = JSON.parseObject(jsonString, Checkpoint.class);
                if(null != checkpoint) {
                    logger.info("Checkpoint file recovered: {}.", checkpoint);
                }
                return checkpoint;
            } else {
                logger.warn("Checkpoint file is NOT found, continue recover...");
            }
        } catch (Throwable t) {
            logger.warn("Recover checkpoint exception, continue recover...", t);
        }
        return null;
    }

    /**
     * 从CheckPoint文件中恢复提交位置。
     * 如果成功恢复，以CheckPoint中记录的位置为准。
     * 如果恢复失败，以最大位置为准。
     */
    private void recoverReplicationPosition(Checkpoint checkpoint) {
        if(null != checkpoint && checkpoint.getVersion() >= Checkpoint.REPLICATION_POSITION_START_VERSION) {
            logger.info("Replication position recovered from the Checkpoint file: {}.", checkpoint.getReplicationPosition());
            long recoveredReplicationPosition = checkpoint.getReplicationPosition();
            if (recoveredReplicationPosition >= 0 && recoveredReplicationPosition <= store.right()) {
                this.replicationPosition = recoveredReplicationPosition;
                logger.info("Replication recovered: {}, store: {}.", recoveredReplicationPosition,  base.getAbsolutePath());
                return;
            }
        }

        // 以最大位置为准存在数据一致性问题。重启后，有可能将未提交的数据变为已提交。
        // 考虑到之前的版本没有记录提交位置，如果直接置为最小位置store.left()，在下一次提交之前，消息都无法被消费，暂时设为最大位置。
        // TODO: 下一版本应该统一改为最小位置。
        this.replicationPosition = store.right();

        logger.warn("Replication recover failed, using store right position: {} instead, store: {}!",
                store.right(),
                base.getAbsolutePath());
    }

    /**
     * 恢复每个分区的索引文件，并返回indexPosition
     * indexPosition为，所有分区最后一条索引指向的那条消息的尾部offset中，最小的那个offset。
     * 可以保证，indexPosition之前的所有消息的索引都存在。
     *
     * @return 返回需要重建索引的第一条消息偏移量
     **/
    private long recoverPartitions() throws IOException {
        File indexBase = new File(base, "index");
        long indexPosition = store.right();
        // 恢复分区索引

        if (!indexBase.isDirectory()) {
            throw new StoreInitializeException(String.format("Index directory: %s not found! ", indexBase.getAbsolutePath()));
        }

        // 从目录结构中读取所有的分区
        Short[] partitionIndices = loadPartitionIndices(indexBase);

        if (partitionIndices == null) return store.left();

        // 遍历恢复每个分区
        for (short partitionIndex : partitionIndices) {

            // 1. 创建Partition对象，加入到partitionMap中

            File partitionBase = new File(indexBase, String.valueOf(partitionIndex));
            PositioningStore<IndexItem> indexStore =
                    new PositioningStore<>(partitionBase, config.indexStoreConfig, bufferPool, new IndexSerializer());
            // 恢复索引存储
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

            if (indexStore.right() - indexStore.left() > 0) {


                // 2. 如果最后一条索引是批消息的索引，需要检查其完整性

                IndexItem lastIndexItem = indexStore.read(indexStore.right() - IndexItem.STORAGE_SIZE);
                if (lastIndexItem == null) throw new ReadException(
                        String.format("Failed to recover index store %s to position %s, batchRead index failed!",
                                indexStore.base().getAbsolutePath(),
                                Format.formatWithComma(
                                        indexStore.right() - IndexItem.STORAGE_SIZE)));
                // 检查对应的消息是否批消息，如果是批消息检查这一批消息的索引的完整性，如不完整直接截掉这个批消息的已存储的所有索引
                verifyBatchMessage(lastIndexItem, indexStore, store);

                // 如果indexPosition大于当前分区索引的最大消息位置， 向前移动indexPosition
                long indexedMessagePosition = lastIndexItem.getOffset();

                logger.info("Topic: {}, group: {}, partition: {}, maxIndexedMessageOffset: {}.", topic,
                        partitionGroup, partitionIndex, Format.formatWithComma(indexedMessagePosition));

                if (indexPosition > indexedMessagePosition) {
                    logger.info("Topic: {}, group: {}, set indexPosition from {} to {}.",
                            topic, partitionGroup, Format.formatWithComma(indexPosition),
                            Format.formatWithComma(indexedMessagePosition));
                    indexPosition = indexedMessagePosition;
                }
            } else {
                indexPosition = store.left();
            }
        }

        return indexPosition;
    }

    /**
     * 根据上一条索引来验证这条索引的合法性
     * @param current 当前索引
     * @param previous 上一条索引
     * @return true 合法
     */
    private boolean verifyCurrentIndex(IndexItem current, IndexItem previous) {
        return current.getLength() > 0 && current.getOffset() > previous.getOffset();
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

    private void verifyBatchMessage(IndexItem lastIndexItem, PositioningStore<IndexItem> indexStore, PositioningStore<ByteBuffer> store) throws IOException {

        if (lastIndexItem.getOffset() < store.right()) {
            ByteBuffer msg = store.read(lastIndexItem.getOffset());
            if (BatchMessageParser.isBatch(msg)) {
                short batchSize = BatchMessageParser.getBatchSize(msg);
                long startIndex = MessageParser.getLong(msg, MessageParser.INDEX);

                if (indexStore.right() < (batchSize + startIndex) * IndexItem.STORAGE_SIZE) {
                    logger.info("Incomplete batch message indices found, roll back index store to {}, " +
                                    "index: {}, message position: {}, store: {}.",
                            Format.formatWithComma(startIndex * IndexItem.STORAGE_SIZE),
                            Format.formatWithComma(lastIndexItem.getIndex()),
                            Format.formatWithComma(lastIndexItem.getOffset()),
                            indexStore.base().getAbsolutePath());
                    indexStore.setRight(startIndex * IndexItem.STORAGE_SIZE);
                }
            }
        }
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
            if (indexItem.getOffset() >= commitPosition()) {
                continue;
            }
            try {
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

    /**
     * 写入一批消息
     * @param byteBuffers 存放消息的buffer
     * @return 这批消息的索引序号
     * @throws IOException 发生IO异常时抛出
     */
    private long[] write(ByteBuffer... byteBuffers) throws IOException {

        // 记录Journal和每个分区当前位置，用于回滚
        long start = store.right();
        // TODO 优化：不必每次都创建这个Map，直接更新Map中的位置即可
        Map<Short, Long> partitionSnapshot = createPartitionSnapshot();

        long position = start;
        long[] indices = new long[byteBuffers.length];

        try {
            for (int i = 0, byteBuffersLength = byteBuffers.length; i < byteBuffersLength; i++) {
                ByteBuffer byteBuffer = byteBuffers[i].slice();

                // 检查消息是否超长
                if (byteBuffer.remaining() > config.maxMessageLength) {
                    throw new WriteException(String.format("Message too large! Message length: %d, limit: %d", byteBuffer.remaining(), config.maxMessageLength));
                }
                // 生成索引
                IndexItem indexItem = IndexItem.parseMessage(byteBuffer, position);
                Partition partition = partitionMap.get(indexItem.getPartition());
                indices[i] = partition.store.right() / IndexItem.STORAGE_SIZE;
                // 把索引序号写入消息中
                MessageParser.setLong(byteBuffer, MessageParser.INDEX, indices[i]);
                indexItem.setIndex(indices[i]);

                // 写入消息
                position = store.append(byteBuffer);
                updateLastEntryTerm(byteBuffer);

                // 写入索引
                if (BatchMessageParser.isBatch(byteBuffer)) {
                    short batchSize = BatchMessageParser.getBatchSize(byteBuffer);
                    indexItem.setBatchMessage(true);
                    indexItem.setBatchMessageSize(batchSize);
                }
                writeIndex(indexItem, partition.store);

                // 唤醒刷盘线程
                flushLoopThread.wakeup();
            }
        } catch (Throwable t) {
            onWriteException(start, partitionSnapshot , t);
            throw t;
        }
        return indices;
    }

    private Map<Short, Long> createPartitionSnapshot() {
        return partitionMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().store.right()));
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

    /**
     * 从写入请求队列中获取写入请求，单线程写入消息。
     */
    private void write() {
        WriteCommand writeCommand = null;

        // 加写锁，防止Leader切换的时候，和复制线程并发写入
        if(!writeLock.tryLock()) {
            throw new IllegalStateException("Acquire write lock failed!");
        }
        // 检查Store状态
        verifyState(true);

        try {
            // 获取写入请求
            writeCommand = writeCommandCache.take();

            // 记录Metric
            if (null != produceMetric) {
                produceMetric.addTraffic("WriteTraffic", Arrays.stream(writeCommand.messages).mapToInt(ByteBuffer::remaining).sum());
            }
            long t0 = System.nanoTime();

            // 如果内存中脏数据过多，需要等待刷盘线程
            if (waitForFlush()) {
                // 超时，写入失败
                if (writeCommand.eventListener != null) {
                    writeCommand.eventListener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_TIMEOUT, null));
                }
            } else {
                // 继续写入
                long[] indices = write(writeCommand.messages);
                // 处理回调
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
            logger.warn("Write failed, cause: disk full! Store: {}.", base.getAbsolutePath());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IllegalStateException e) {
            if (null != writeCommand && writeCommand.eventListener != null)
                writeCommand.eventListener.onEvent(new WriteResult(JoyQueueCode.CY_STATUS_ERROR, null));
            logger.warn("Write failed, cause: store disabled! Store: {}.", base.getAbsolutePath());
        } catch (Throwable t) {
            if (null != writeCommand && writeCommand.eventListener != null)
                writeCommand.eventListener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_FAILED, null));
            logger.warn("Write failed, cause: exception! Store: {}.", base.getAbsolutePath(), t);
        } finally {
            writeLock.unlock();
        }
    }

    private void verifyState(boolean expectedState) {
        if(enabled.get() != expectedState) {
            throw new IllegalStateException();
        }
    }

    /**
     * 如果内存中脏数据过多，需要等待刷盘线程。
     *
     * 满足如下任一一个条件退出等待并返回：
     * 1. 一部分数据已经写入磁盘，脏数据大小小于config.maxDirtySize，返回false；
     * 2. 超时，返回true；
     * @return true：超时。false：脏数据大小小于config.maxDirtySize。
     */
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
        CallbackPositioningBelt belt = callbackMap.get(writeCommand.qosLevel);
        if(null != belt) {
            belt.put(callback);
        }
    }

    private void flush() {
        if(flushLock.tryLock()) {
            try {
                boolean flushed;
                do {
                    long t0 = System.nanoTime();
                    long before = store.flushPosition();

                    //执行Journal和Index刷盘操作，返回值标识是否有数据写入了磁盘
                    flushed = store.flush() | flushIndices();

                    // 记录Metric
                    if (null != produceMetric && flushed) {
                        long t1 = System.nanoTime();
                        produceMetric.addTraffic("FlushTraffic", store.flushPosition() - before);
                        produceMetric.addLatency("FlushLatency", t1 - t0);
                        produceMetric.addCounter("FlushCount", 1);
                    }

                    // 执行回调
                    if (flushed) {
                        callbackMap.get(QosLevel.PERSISTENCE).callbackBefore(flushPosition());
                    }

                    // 定期更新CheckPoint
                    flushCheckpointPeriodically();
                } while (flushed && isStarted());
            } catch (IOException e) {
                logger.warn("Exception:", e);
            } finally {
                flushLock.unlock();
            }
        }
    }

    private boolean flushIndices() {
        try {
            boolean flushed = false;
            for (Partition partition : partitionMap.values()) {
                flushed = partition.store.flush() || flushed;
            }
            return flushed;
        } catch (Exception e) {
            logger.warn("Exception: ", e);
        }
        return false;
    }

    private boolean isDiskFull() {
        long timestamp = lastCheckDiskSpaceTimestamp.get();
        if(SystemClock.now() - timestamp > CHECK_DISK_SPACE_COOL_DOWN && // 超过冷却时间了
                lastCheckDiskSpaceTimestamp.compareAndSet(timestamp, SystemClock.now())) { // 避免并发修改 isDiskFull
            isDiskFull = store.isDiskFull();
        }
        return isDiskFull;
    }

    /**
     * 写入消息入口方法，生产者发送的消息都是通过这个方法写入存储中。
     * 此方法为异步写入，不会阻塞调用线程，写入结果通过回调接口eventListener返回。
     *
     * 保证原子性，即同一批写入请求的消息，保证要么都写入成功，要么都写入失败。
     * 同一线程内，保证顺序：在统一线程内先后写入的消息，保证按照调用顺序写入。
     *
     * @param qosLevel QOS 级别
     * @param eventListener 回调接口
     * @param writeRequests 写入请求数组
     */
    void asyncWrite(QosLevel qosLevel, EventListener<WriteResult> eventListener, WriteRequest... writeRequests) {
        // 检查服务是否启动
        ensureStarted();

        // 检查磁盘空间
        if(isDiskFull()) {
            if (eventListener != null)
                eventListener.onEvent(new WriteResult(JoyQueueCode.SE_DISK_FULL, null));
            return;
        }

        // 检查服务状态
        if (!enabled.get())
            throw new WriteException(String.format("Store disabled! topic: %s, partitionGroup: %d.", topic, partitionGroup));

        // 解析WriteRequests，构建写入的数据
        ByteBuffer[] messages = new ByteBuffer[writeRequests.length];
        for (int i = 0, writeRequestsLength = writeRequests.length; i < writeRequestsLength; i++) {
            WriteRequest writeRequest = writeRequests[i];

            ByteBuffer byteBuffer = writeRequest.getBuffer();

            // 检查长度是否正确
            int length;
            if ((length = MessageParser.getInt(byteBuffer, MessageParser.LENGTH)) != byteBuffer.remaining()) {
                throw new WriteException(String.format("Message length check error! Expect: %d, actual: %d", length, byteBuffer.remaining()));
            }

            // 检查写入的分区是否存在
            if (!partitionMap.containsKey(writeRequest.getPartition())) {
                throw new WriteException(String.format("No partition %d in partition group %d of topic %s!", writeRequest.getPartition(), partitionGroup, topic));
            }

            // 补齐消息中的属性
            // 分区
            MessageParser.setShort(byteBuffer, MessageParser.PARTITION, writeRequest.getPartition());
            // 任期
            MessageParser.setInt(byteBuffer, MessageParser.TERM, term);
            // 存储时间：与发送时间的差值
            MessageParser.setInt(byteBuffer, MessageParser.STORAGE_TIMESTAMP,
                    (int) (SystemClock.now() - MessageParser.getLong(byteBuffer, MessageParser.CLIENT_TIMESTAMP)));
            messages[i] = writeRequest.getBuffer();
        }

        // 构建写入请求对象
        WriteCommand writeCommand = new WriteCommand(qosLevel, eventListener, messages);
        // 放入队列中，如果队列满，阻塞等待
        try {
            this.writeCommandCache.put(writeCommand);
        } catch (InterruptedException e) {
            logger.warn("Exception: ", e);
            if (eventListener != null)
                eventListener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_FAILED, null));
        }
        // 如果QosLevel.RECEIVE，这里就可以给客户端返回写入成功的响应了。
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


    long clean(long time, Map<Short, Long> partitionAckMap, boolean keepUnconsumed) throws IOException {
        long deletedSize = 0L;

        // 计算最小索引的消息位置
        long minMessagePosition = -1L;
        for (Map.Entry<Short, Long> partition : partitionAckMap.entrySet()) {
            Short p = partition.getKey();
            long minPartitionIndex = partition.getValue();
            PositioningStore<IndexItem> indexStore = indexStore(p);
            if (indexStore != null) {
                if (minPartitionIndex != Long.MAX_VALUE && keepUnconsumed) {
                    minPartitionIndex *= IndexItem.STORAGE_SIZE;
                } else {
                    minPartitionIndex = Long.MAX_VALUE;
                }
                if (time <= 0) {
                    // 依次删除每个分区p索引中最左侧的文件 满足当前分区p的最小消费位置之前的文件块
                    if (indexStore.fileCount() > 1 && indexStore.meetMinStoreFile(minPartitionIndex) > 1) {
                        deletedSize += indexStore.physicalDeleteLeftFile();
                        if (logger.isDebugEnabled()){
                            logger.info("Delete PositioningStore physical index file by size, partition: <{}>, offset position: <{}>", p, minPartitionIndex);
                        }
                    }
                } else {
                    // 依次删除每个分区p索引中最左侧的文件 满足当前分区p的最小消费位置之前的以及最长时间戳的文件块
                    if (indexStore.fileCount() > 1 && indexStore.meetMinStoreFile(minPartitionIndex) > 1 && hasEarly(indexStore,time)) {
                        deletedSize += indexStore.physicalDeleteLeftFile();
                        if (logger.isDebugEnabled()){
                            logger.info("Delete PositioningStore physical index file by time, partition: <{}>, offset position: <{}>", p, minPartitionIndex);
                        }
                    }
                }

                try {
                    long storeMinMessagePosition = indexStore.read(indexStore.left()).getOffset();
                    if (minMessagePosition < 0 || minMessagePosition > storeMinMessagePosition) {
                        minMessagePosition = storeMinMessagePosition;
                    }
                } catch (PositionOverflowException ignored) {
                }
            }
        }

        if (minMessagePosition >= 0) {
            deletedSize += store.physicalDeleteTo(minMessagePosition);
            if (logger.isDebugEnabled()) {
                logger.info("Delete PositioningStore physical message file, offset position: <{}>", minMessagePosition);
            }
        }

        return deletedSize;
    }

    /**
     *
     * @param indexStore  partition index store
     * @param time 查询时间
     * @return true if partition 的最早消息时间小于指定时间
     *
     **/
    private  boolean hasEarly(PositioningStore<IndexItem> indexStore,long time) throws IOException{
        long left=indexStore.left();
        IndexItem item=indexStore.read(left);
        ByteBuffer message=store.read(item.getOffset());
        // message send time
        long clientTimestamp=MessageParser.getLong(message,MessageParser.CLIENT_TIMESTAMP);
        long offset=MessageParser.getInt(message,MessageParser.STORAGE_TIMESTAMP);
        return clientTimestamp + offset < time;
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
    protected void doStart() throws Exception {
        if (config.printMetricIntervalMs > 0) {
            metricThread.start();
        }
        startFlushThread();
        if (enabled.get()) {
            startWriteThread();
        }
    }


    private void startFlushThread() {
       flushLoopThread.start();
    }


    private void startWriteThread() {

        this.writeLoopThread.start();
    }

    @Override
    protected void doStop() {
        try {

            logSafe("Stopping store {}-{}...", topic, partitionGroup);
            logSafe("Waiting for flush finished {}-{}...", topic, partitionGroup);
            try {
                while (!isAllStoreClean()) {
                    //noinspection BusyWait
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            stopWriteThread();
            logSafe("Stopping flush thread {}-{}...", topic, partitionGroup);

            stopFlushThread();
            flushCheckpoint();

            if (config.printMetricIntervalMs > 0) {
                logSafe("Stopping metric threads {}-{}...", topic, partitionGroup);
                metricThread.stop();
            }
            System.out.println("Store stopped. " + base.getAbsolutePath());
            logSafe("Store stopped {}-{}.", topic, partitionGroup);
        } catch (Throwable t) {
            logger.error(t.getMessage(),t);
        }
    }


    private void logSafe(String format, Object... arguments) {
        try {
            logger.info(format, arguments);
        } catch (Throwable t) {
            System.out.println(
                    String.format(
                        format.replaceAll("\\{}", "%s"), arguments
                    )
            );
        }
    }
    private boolean isAllStoreClean() {
        return Stream.concat(Stream.of(store), partitionMap.values().stream().map(partition -> partition.store)).allMatch(PositioningStore::isClean);
    }


    private void stopFlushThread() {
        flushLoopThread.stop();
    }

    private void stopWriteThread() {
        writeLoopThread.stop();
    }

    public long getLeftIndex(short partition) {
        long index = -1;
        Partition p = partitionMap.get(partition);
        if (null != p) {
            index = p.store.left() / IndexItem.STORAGE_SIZE;
        }
        return index;
    }

    public long getLeftIndexAndCheck(short partition) {
        rollbackLock.readLock().lock();
        try {
            if (!enabled.get()) {
                throw new ReadException(String.format("Store disabled! topic: %s, partitionGroup: %d.", topic, partitionGroup));
            }
            return getLeftIndex(partition);
        } finally {
            rollbackLock.readLock().unlock();
        }
    }

    public long getRightIndex(short partition) {
        long index = -1;
        Partition p = partitionMap.get(partition);
        if (null != p) {
            index = p.store.right() / IndexItem.STORAGE_SIZE;
        }
        return index;
    }

    public long getRightIndexAndCheck(short partition) {
        rollbackLock.readLock().lock();
        try {
            if (!enabled.get()) {
                throw new ReadException(String.format("Store disabled! topic: %s, partitionGroup: %d.", topic, partitionGroup));
            }
            return getRightIndex(partition);
        } finally {
            rollbackLock.readLock().unlock();
        }
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
        if (isStarted() && enabled.compareAndSet(false, true)) {
            startWriteThread();
        }
    }

    @Override
    public void disable() {
        if (enabled.get()) {
            stopWriteThread();
            writeCommandCache.clear();
            enabled.set(false);
        }
    }

    @Override
    public void setRightPosition(long position) throws IOException {
        flushLock.waitAndLock();
        try {
            rollback(position);
        } finally {
            flushLock.unlock();
        }
    }


    @Override
    public void clear(long position) throws IOException {
        flushLock.waitAndLock();
        try {
            for (Partition partition : partitionMap.values()) {
                partition.store.setRight(0L);
            }
            store.clear(position);
        } finally {
            flushLock.unlock();
        }
    }



    private void rollback(long position) throws IOException {
        rollbackLock.writeLock().lock();
        try {
            if(indexPosition > position) {
                indexPosition = position;
                flushCheckpoint();
            }
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

            resetLastEntryTerm();
        } finally {
            rollbackLock.writeLock().unlock();
        }
    }

    private void flushCheckpointPeriodically() throws IOException {
        if(SystemClock.now() > lastFlushCheckpointTimestamp + FLUSH_CHECKPOINT_INTERVAL_MS) {
            flushCheckpoint();
            lastFlushCheckpointTimestamp = SystemClock.now();
        }
    }
    private void flushCheckpoint() throws IOException {
        Checkpoint checkpoint = new Checkpoint(indexPosition, replicationPosition, partitionMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().store.right() / IndexItem.STORAGE_SIZE)));
        byte [] serializedData = JSON.toJSONString(checkpoint,
                SerializerFeature.PrettyFormat, SerializerFeature.DisableCircularReferenceDetect).getBytes(StandardCharsets.UTF_8);

        File checkpointFile =  new File(base, CHECKPOINT_FILE);
        try(FileOutputStream fos = new FileOutputStream(checkpointFile)) {
            fos.write(serializedData);
        }
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

    /**
     * 追加写入一段消息数据，给定的ByteBuffer必须满足如下条件：
     * 1. 连续且完整的多条消息。
     * 2. 这些消息具有相同的term。
     * 3. 消息中同一分区内的索引序号必须连续，并且与同分区已有消息的索引序号连续。
     *
     * @param byteBuffer 待写入的ByteBuffer，将position至limit之间的数据写入存储。
     *                   写入完成后， position == limit。
     * @return {@link #rightPosition()}
     * @throws IOException 发生IO错误
     * @throws TimeoutException 等待写入超时，需要重试。
     */
    @Override
    public long appendEntryBuffer(ByteBuffer byteBuffer) throws IOException, TimeoutException {
        ensureStarted();
        if(!writeLock.tryLock()) {
            throw new IllegalStateException("Acquire write lock failed!");
        }
        try {
            verifyState(false);

            long t0 = System.nanoTime();
            if (waitForFlush()) {
                throw new TimeoutException("Wait for flush timeout! The broker is too much busy to write data to disks.");
            }
            // 记录消息和索引位置，用于失败回滚
            long start = store.right();
            Map<Short, Long> partitionSnapshot = createPartitionSnapshot();

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
                        // 如果当前分区没有消息，以这条消息记录的索引序号为准
                        partition.store.setRight(indexItem.getIndex() * IndexItem.STORAGE_SIZE);
                    } else if (indexItem.getIndex() * IndexItem.STORAGE_SIZE != partition.store.right()) {
                        // 检查索引序号在分区内是否连续
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
                    updateLastEntryTerm(byteBuffer);
                    byteBuffer.position(byteBuffer.position() + indexItem.getLength());
                    counter++;
                }

                // 记录Metric
                if (null != produceMetric) {
                    long t1 = System.nanoTime();
                    produceMetric.addTraffic("WriteTraffic", size);
                    produceMetric.addLatency("WriteLatency", t1 - t0);
                    produceMetric.addCounter("WriteCount", counter);

                }
                return position;
            } catch (Throwable t) {
                onWriteException(start, partitionSnapshot , t);
                throw t;
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void ensureStarted() {
        if(!isStarted()) {
            throw new IllegalStateException("Store stopped.");
        }
    }

    private void updateLastEntryTerm(ByteBuffer byteBuffer) {
        int term = MessageParser.getInt(byteBuffer, MessageParser.TERM);
        if(term >= 0) {
            if(term > lastEntryTerm) lastEntryTerm = term;
        } else {
            throw new WriteException(String.format("Invalid term %d at position %d!", term, byteBuffer.position()));
        }
    }

    private void onWriteException(long start, Map<Short, Long> partitionSnapshot, Throwable t) {
        try {
            rollback(start, partitionSnapshot);
        } catch (Throwable e) {
            logger.warn("Rollback failed, rollback to position: {}, topic={}, partitionGroup={}.", start, topic, partitionGroup, e);
        }
        if (t instanceof DiskFullException) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {
            }
        } else {
            logger.warn("Write failed, rollback to position: {}, topic={}, partitionGroup={}.", start, topic, partitionGroup, t);
        }
    }

    private void rollback(long position, Map<Short, Long> partitionSnapshot) throws IOException{
        flushLock.waitAndLock();
        try {
            // 回滚分区索引
            partitionSnapshot.forEach((partition, snapshotPosition) -> {
                try {
                    partitionMap.get(partition).store.setRight(snapshotPosition);
                } catch (Throwable e) {
                    logger.warn("Rollback partition failed! " +
                                    "topic: {}, group: {}, partition: {}, rollback position: {}, current position: {}, store: {}.",
                            topic, partitionGroup, partition, snapshotPosition, partitionMap.get(partition).store.right(),
                            base.getAbsoluteFile(), e);
                }
            });
            // 回滚indexPosition
            indexPosition = position;
            try {
                flushCheckpoint();
            } catch (Throwable ignored){}
            // 回滚commit log
            store.setRight(position);
        } finally {
            flushLock.unlock();
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


    @Override
    public int lastEntryTerm(){
        return lastEntryTerm;
    }

    /**
     * LEADER 收到半数以上回复后，调用此方法提交
     */
    @Override
    public void commit(long position) {
        CallbackPositioningBelt belt;
        if (position > replicationPosition) {
            replicationPosition = position;
            belt = this.callbackMap.get(QosLevel.REPLICATION);
            belt.callbackBefore(this.commitPosition());

        }

        belt = this.callbackMap.get(QosLevel.ALL);
        belt.callbackBefore(Math.min(this.flushPosition(), this.commitPosition()));

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
        public static final int DEFAULT_WRITE_REQUEST_CACHE_SIZE = 128;
        public static final long DEFAULT_FLUSH_INTERVAL_MS = 50L;
        public static final boolean DEFAULT_FLUSH_FORCE = true;
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

        private final PositioningStore.Config storeConfig;
        private final PositioningStore.Config indexStoreConfig;

        public Config() {

            this(DEFAULT_MAX_MESSAGE_LENGTH, DEFAULT_WRITE_REQUEST_CACHE_SIZE, DEFAULT_FLUSH_INTERVAL_MS,
                    DEFAULT_WRITE_TIMEOUT_MS, DEFAULT_MAX_DIRTY_SIZE, DEFAULT_PRINT_METRIC_INTERVAL_MS,
                    new PositioningStore.Config(PositioningStore.Config.DEFAULT_FILE_DATA_SIZE),
                    // 索引在读取的时候默认加载到内存中
                    new PositioningStore.Config(PositioningStore.Config.DEFAULT_FILE_DATA_SIZE, true, DEFAULT_FLUSH_FORCE));
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

    static class CallbackPositioningBelt {
        private final ConcurrentLinkedQueue<Callback> queue = new ConcurrentLinkedQueue<>();
        private final AtomicLong callbackPosition = new AtomicLong(0L);

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

            try {
                if(position > callbackPosition.get()) {
                    callbackPosition.set(position);
                    while (getFirst().position <= position) {
                        Callback callback = removeFirst();
                        callback.listener.onEvent(new WriteResult(JoyQueueCode.SUCCESS, callback.indices));
                    }
                }
                long deadline = SystemClock.now() - EVENT_TIMEOUT_MILLS;
                while (getFirst().timestamp < deadline) {
                    Callback callback = removeFirst();
                    callback.listener.onEvent(new WriteResult(JoyQueueCode.SE_WRITE_TIMEOUT, null));
                }
            } catch (NoSuchElementException ignored) {}
        }

        void put(Callback callback) {
            addLast(callback);
            if (callback.position <= callbackPosition.get() && remove(callback)) {
                callback.listener.onEvent(new WriteResult(JoyQueueCode.SUCCESS, callback.indices));
            }
        }
    }

}
