package com.jd.journalq.store;

import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.store.file.PositioningStore;
import com.jd.journalq.store.file.RollBackException;
import com.jd.journalq.store.file.StoreMessageSerializer;
import com.jd.journalq.store.file.WriteException;
import com.jd.journalq.store.index.IndexItem;
import com.jd.journalq.store.index.IndexSerializer;
import com.jd.journalq.store.message.BatchMessageParser;
import com.jd.journalq.store.message.MessageParser;
import com.jd.journalq.store.nsm.VirtualThread;
import com.jd.journalq.store.nsm.VirtualThreadExecutor;
import com.jd.journalq.store.replication.ReplicableStore;
import com.jd.journalq.store.utils.PreloadBufferPool;
import com.jd.journalq.store.utils.ThreadSafeFormat;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.concurrent.LoopThread;
import com.jd.journalq.toolkit.lang.LifeCycle;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liyue25
 * Date: 2018/8/13
 */
public class PartitionGroupStoreManager implements ReplicableStore, LifeCycle, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PartitionGroupStoreManager.class);
    private final PositioningStore<ByteBuffer> store;
    private final File base;
    private final String topic;
    private final int partitionGroup;
    private static final long EVENT_TIMEOUT_MILLS = 60 * 1000L;
    private final CallbackPositioningBelt flushCallbackBelt, commitCallbackBelt;
    private final Map<Short, Partition> partitionMap = new ConcurrentHashMap<>();
    private final Config config;
    private final QosStore [] qosStores =
            {   new QosStore(this, QosLevel.ONE_WAY),
                new QosStore(this, QosLevel.RECEIVE),
                new QosStore(this, QosLevel.PERSISTENCE),
                new QosStore(this, QosLevel.REPLICATION)
            };
    private long replicationPosition;
    private long indexPosition;
    private AtomicBoolean started, enabled;
    private int term; // 当前轮次
    private final ScheduledExecutorService scheduledExecutorService;
    private final PreloadBufferPool bufferPool;
    private final VirtualThreadExecutor virtualThreadPool;

    private final VirtualThread writeVirtualThread = this::writeVT;
    private final VirtualThread callbackVirtualThread = this::callbackVT;
    private final LoopThread writeLoopThread;
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
        this.store = new PositioningStore<>(base, config.storeConfig,bufferPool, storeMessageSerializer);
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
                .sleepTime(0,0)
                .onException(e -> logger.warn("Write Exception: ", e))
                .build();
    }

    public void recover() throws IOException {
        logger.info("Recovering message store...");
        store.recover();
        logger.info("Recovering index store...");
        indexPosition = recoverPartitions();
        logger.info("Building indices ...");
        recoverIndices();
    }

    private void recoverIndices() throws IOException {

        // 删除indexPosition之后的所有索引
        for(Partition partition : partitionMap.values()) {
            partition.rollbackTo(indexPosition);
        }

        // 从indexPosition到store.right()重新构建索引
        while (indexPosition < store.right()) {
            ByteBuffer byteBuffer = store.read(indexPosition);
            if(null == byteBuffer) throw new ReadException(String.format("Read log failed! store: %s, position: %d.", store.base().getAbsolutePath(), indexPosition));
            IndexItem indexItem = IndexItem.parseMessage(byteBuffer, indexPosition);
            Partition partition = partitionMap.get(indexItem.getPartition());
            PositioningStore<IndexItem> indexStore = partition.store;

            if(indexStore.right() == 0) {
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
                if(pi.getOffset() != indexPosition) {
                    throw new WriteException(
                            String.format(
                                    "Index mismatch, store: %s, partition: %d, next index of the partition: %s，index in log: %s, log position: %s, log: \n%s",
                                    this.base, indexItem.getPartition(),

                                    ThreadSafeFormat.formatWithComma(storeIndex),
                                    ThreadSafeFormat.formatWithComma(indexItem.getIndex()),
                                    ThreadSafeFormat.formatWithComma(indexPosition),
                                    MessageParser.getString(byteBuffer)));
                }
            } else if (indexItem.getIndex() > storeIndex) {
                throw new WriteException(
                        String.format(
                                "Index must be continuous, store: %s, partition: %d, next index of the partition: %s，index in log: %s, log position: %s, log: \n%s",
                                this.base, indexItem.getPartition(),

                                ThreadSafeFormat.formatWithComma(storeIndex),
                                ThreadSafeFormat.formatWithComma(indexItem.getIndex()),
                                ThreadSafeFormat.formatWithComma(indexPosition),
                                MessageParser.getString(byteBuffer)));
            }

            if(indexStore.right() - indexStore.flushPosition() >= 10 * 1024 * 1024) {
                indexStore.flush();
                logger.info("Write position: {}, index position: {}", store.right(), indexPosition);
            }
        }

        for(Partition partition: partitionMap.values()) {
            PositioningStore<IndexItem> indexStore = partition.store;
            if(indexStore.right() > indexStore.flushPosition()) {
                indexStore.flush();
            }
        }
    }

    private void rollbackPartitions(long messagePosition) throws IOException {
        for(Partition partition :partitionMap.values()) {
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

        if(partitionIndices == null) return indexPosition;

        for (short partitionIndex : partitionIndices) {

            // 1. 创建Partition对象，加入到partitionMap中

            File partitionBase = new File(indexBase, String.valueOf(partitionIndex));
            PositioningStore<IndexItem> indexStore =
                    new PositioningStore<>(partitionBase, config.indexStoreConfig, bufferPool, new IndexSerializer());
            indexStore.recover();
            partitionMap.put(partitionIndex, new Partition(indexStore));

            if(indexStore.right() > 0) {


                // 2. 如果最后一条索引是批消息的索引，需要检查其完整性

                IndexItem lastIndexItem = indexStore.read(indexStore.right() - IndexItem.STORAGE_SIZE);
                if (lastIndexItem == null) throw new ReadException(
                        String.format("Failed to recover index store %s to position %s, batchRead index failed!",
                                indexStore.base().getAbsolutePath(),
                                ThreadSafeFormat.formatWithComma(
                                        indexStore.right() - IndexItem.STORAGE_SIZE)));
                // 检查对应的消息是否批消息，如果是批消息检查这一批消息的索引的完整性，如不完整直接截掉这个批消息的已存储的所有索引
                lastIndexItem = verifyBatchMessage(lastIndexItem, indexStore, store);

                // 如果indexPosition大于当前分区索引的最大消息位置， 向前移动indexPosition
                long indexedMessagePosition = lastIndexItem.getOffset() + lastIndexItem.getLength();

                logger.info("Topic: {}, group: {}, partition: {}, maxIndexedMessageOffset: {}.", topic,
                        partitionGroup, partitionIndex, ThreadSafeFormat.formatWithComma(indexedMessagePosition));

                if (indexPosition > indexedMessagePosition) {
                    logger.info("Topic: {}, group: {}, set indexPosition from {} to {}.",
                            topic, partitionGroup, ThreadSafeFormat.formatWithComma(indexPosition),
                            ThreadSafeFormat.formatWithComma(indexedMessagePosition));
                    indexPosition = indexedMessagePosition;
                }
            }
        }

        return indexPosition;
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

        if(lastIndexItem.getOffset() < store.right()) {
            ByteBuffer msg = store.read(lastIndexItem.getOffset());
            if(BatchMessageParser.isBatch(msg)) {
                short batchSize = BatchMessageParser.getBatchSize(msg);
                long startIndex = MessageParser.getLong(msg, MessageParser.INDEX);

                if(indexStore.right() < (batchSize + startIndex) * IndexItem.STORAGE_SIZE) {
                    logger.info("Incomplete batch message indices found, roll back index store to {}.",
                            ThreadSafeFormat.formatWithComma(startIndex * IndexItem.STORAGE_SIZE));
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
            if(!partitionBase.renameTo(new File(partitionBase.getParent(), partitionBase.getName() + ".d." + System.currentTimeMillis()))){
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
        ReadResult readResult = new ReadResult();
        checkPartition(partition);
        PositioningStore<IndexItem> indexStore = partitionMap.get(partition).store;
        List<IndexItem> indexItemList = indexStore.batchRead(index * IndexItem.STORAGE_SIZE, count);

        long size = 0L;
        readResult.setEop(indexItemList.size() < count);
        List<ByteBuffer> buffers = new ArrayList<>(count);
        IndexItem lastIndexItem = null;
        for (IndexItem indexItem : indexItemList) {
            // 如果索引的offset和上一条相同，说明它们是同一批消息，直接跳过即可
            if(null != lastIndexItem && indexItem.getOffset() == lastIndexItem.getOffset()) {
                continue;
            }
            ByteBuffer log = store.read(indexItem.getOffset(), indexItem.getLength());
            if(null != log) {
                if (maxSize <= 0 || (size += log.remaining()) < maxSize) {
                    buffers.add(log);
                } else {
                    break;
                }
            } else {
                throw new ReadException(String.format("Read log failed! store: %s, position: %d.", store.base().getAbsolutePath(), indexItem.getOffset()));
            }
            lastIndexItem = indexItem;
        }
        readResult.setMessages(buffers.toArray(new ByteBuffer[0]));
        readResult.setCode(JMQCode.SUCCESS);
        return readResult;
    }

    private void checkPartition(short partition) {
        if(!partitionMap.containsKey(partition))
            throw new ReadException(String.format("No such partition: %d in topic: %s, partition group: %d.", partition, topic, partitionGroup));
    }

    private long [] write(ByteBuffer... byteBuffers) throws IOException {
        long start = store.right();
        long position = start;
        long [] indices = new long [byteBuffers.length];
        try {
            for (int i = 0, byteBuffersLength = byteBuffers.length; i < byteBuffersLength; i++) {
                ByteBuffer byteBuffer = byteBuffers[i];
                if(byteBuffer.remaining() > config.maxMessageLength) {
                    throw new WriteException(String.format("Message too large! Message length: %d, limit: %d", byteBuffer.remaining(), config.maxMessageLength));
                }
                // 生成索引
                IndexItem indexItem = IndexItem.parseMessage(byteBuffer, position);
                Partition partition = partitionMap.get(indexItem.getPartition());
                indices[i] = partition.store.right() / IndexItem.STORAGE_SIZE;

                MessageParser.setLong(byteBuffer, MessageParser.INDEX, indices[i]);
                indexItem.setIndex(indices[i]);

                // 写入消息
                position = store.append(byteBuffer);

                // 写入索引

                if (BatchMessageParser.isBatch(byteBuffer)) {
                    short batchSize = BatchMessageParser.getBatchSize(byteBuffer);
                    indexItem.setBatchMessage(true);
                    indexItem.setBatchMessageSize(batchSize);
                }
                writeIndex(indexItem, partition.store);

            }
        } catch (Throwable t) {
            logger.warn("Write failed, rollback to position: {}, topic={}, partitionGroup={}.", start, topic, partitionGroup, t);
            try {
                setRightPosition(start, config.writeTimeoutMs);
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

    private boolean writeVT() {
        boolean ret = false;
        WriteCommand writeCommand;
        if (null != (writeCommand = writeCommandCache.poll())) {
            try {
                if(waitForFlush()) {
                    writeCommand.eventListener.onEvent(new WriteResult(JMQCode.SE_WRITE_TIMEOUT, null));
                } else {
                    long[] indices = write(writeCommand.messages);
                    handleCallback(writeCommand, store.right(), indices);
                }
                ret = true;
            } catch (Throwable t) {
                if (writeCommand.eventListener != null)
                    writeCommand.eventListener.onEvent(new WriteResult(JMQCode.SE_WRITE_FAILED, null));
            }
        }
        return ret;
    }

    private void write() throws IOException, InterruptedException {
        WriteCommand writeCommand = null;
        try {
            writeCommand = writeCommandCache.take();

            if (waitForFlush()) {
                writeCommand.eventListener.onEvent(new WriteResult(JMQCode.SE_WRITE_TIMEOUT, null));
            } else {
                long[] indices = write(writeCommand.messages);
                handleCallback(writeCommand, store.right(), indices);
            }

        } catch (Throwable t) {
            if (null != writeCommand && writeCommand.eventListener != null)
                writeCommand.eventListener.onEvent(new WriteResult(JMQCode.SE_WRITE_FAILED, null));
            throw t;
        }
    }

    private boolean waitForFlush() {

            long t0 = System.currentTimeMillis();
            while (store.right() - store.flushPosition() >= config.maxDirtySize && System.currentTimeMillis() - t0 <= config.writeTimeoutMs) {
                Thread.yield();
            }
            return System.currentTimeMillis() - t0 > config.writeTimeoutMs;
    }


    private void handleCallback(WriteCommand writeCommand,long position, long [] indices) {
        Callback callback = new Callback(writeCommand.qosLevel, writeCommand.eventListener,indices);
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
        }catch (NoSuchElementException ignored){}
        return ret;
    }

    private void evict () {
        store.evict();
        for(Partition p : partitionMap.values()){
            p.store.evict();
        }
    }

    private void flush() {
        try {
            boolean flushed;
            do {
                flushed = store.flush();
                flushIndices();
            } while (flushed);
        }catch (IOException e) {
            logger.warn("Exception:",e);
        }
    }

    private void flushIndices() throws IOException {
        for(Partition partition: partitionMap.values()){
            partition.store.flush();
        }
    }

    private final BlockingQueue<WriteCommand> writeCommandCache;


    void asyncWrite(QosLevel qosLevel, EventListener<WriteResult> eventListener, WriteRequest... writeRequests) {

        if (!enabled.get())
            throw new WriteException(String.format("Store disabled! topic: %s, partitionGroup: %d.", topic, partitionGroup));
        ByteBuffer [] messages = new ByteBuffer[writeRequests.length];
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
                    (int )(SystemClock.now() - MessageParser.getLong(byteBuffer, MessageParser.CLIENT_TIMESTAMP)));
            messages[i] = writeRequest.getBuffer();
        }
        WriteCommand writeCommand = new WriteCommand(qosLevel, eventListener, messages);
        try {
            this.writeCommandCache.put(writeCommand);
        } catch (InterruptedException e) {
            logger.warn("Exception: ", e);
            if(eventListener != null)
                eventListener.onEvent(new WriteResult(JMQCode.SE_WRITE_FAILED, null));
        }

        if(qosLevel == QosLevel.RECEIVE && null != eventListener) {
            eventListener.onEvent(new WriteResult(JMQCode.SUCCESS,null));
        }
    }

    long indexPosition() {
        return indexPosition;
    }

    PositioningStore<IndexItem> indexStore(short partition) {
        return partitionMap.get(partition).store;
    }

    public PositioningStore<ByteBuffer> messageStore() {
        return store;
    }

    public Set<PositioningStore<IndexItem>> meetPositioningStores() {
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

    private ScheduledFuture callbackFeature, flushFeature, evictFeature;

    @Override
    public synchronized void start() {

        startCallbackThread();
        startFlushThread();
        startEvictThread();
        started.set(true);
    }

    private void startEvictThread() {
        evictFeature = scheduledExecutorService.scheduleAtFixedRate(this::evict,
                ThreadLocalRandom.current().nextLong(500L, 1000L),
                config.evictIntervalMs, TimeUnit.MILLISECONDS);
    }
    private void startFlushThread() {

        flushFeature = scheduledExecutorService.scheduleAtFixedRate(this::flush,
                ThreadLocalRandom.current().nextLong(500L, 1000L),
                config.flushIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void startCallbackThread() {
        if(null != virtualThreadPool) {
            this.virtualThreadPool.start(callbackVirtualThread, config.flushIntervalMs, String.format("CallbackThread-%s-%d", topic, partitionGroup));
        } else {
            callbackFeature = scheduledExecutorService.scheduleAtFixedRate(this::callbackVT,
                    ThreadLocalRandom.current().nextLong(500L, 1000L),
                    config.flushIntervalMs, TimeUnit.MILLISECONDS);
        }
    }

    private void startWriteThread() {
        if(null != virtualThreadPool) {
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
                long t0 = System.currentTimeMillis();
                try {
                    while (System.currentTimeMillis() - t0 < stopTimeout &&
                            !isAllStoreClean()) {
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopFlushThread(stopTimeout);
                stopCallbackThread(stopTimeout);
                stopAndWaitScheduledFeature(evictFeature, stopTimeout);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private boolean isAllStoreClean() {
        return Stream.concat(Stream.of(store),partitionMap.values().stream().map(partition -> partition.store)).allMatch(PositioningStore::isClean);
    }

    private void stopCallbackThread(long stopTimeout) throws TimeoutException {
        if(null != virtualThreadPool) {
            safeStop("Stopping callback thread...", callbackVirtualThread);
        } else {
            stopAndWaitScheduledFeature(callbackFeature, stopTimeout);
        }
    }
    private void stopFlushThread(long stopTimeout) throws TimeoutException {
       stopAndWaitScheduledFeature(flushFeature, stopTimeout);
    }

    private void stopWriteThread() {
        if(null != virtualThreadPool) {
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
            e.printStackTrace();
        }
    }

    private void stopAndWaitScheduledFeature(ScheduledFuture scheduledFuture, long timeout) throws TimeoutException {
        if (scheduledFuture != null) {
            long t0 = System.currentTimeMillis();
            while (!scheduledFuture.isDone()) {
                if(System.currentTimeMillis() - t0 > timeout) {
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
    public void disable(long timeoutMs) {
        if (enabled.get()) {
            writeCommandCache.clear();
            stopWriteThread();
            enabled.set(false);
        }
    }

    @Override
    public void setRightPosition(long position, long timeout) throws IOException, TimeoutException {
        stopFlushThread(timeout);
        try {
            rollback(position);
        } finally {
            startFlushThread();
        }
    }

    private void rollback(long position) throws IOException {

        boolean clearIndexStore =  position <= leftPosition() || position > rightPosition();

        // 如果store整个删除干净了，需要把index也删干净
        // FIXME: 考虑这种情况：FOLLOWER被rollback后，所有文件都被删除了，但它有一个非零的writePosition，index是0，
        //  如果被选为LEADER，index是不正确的。
        if(clearIndexStore) {
            for(Partition partition :partitionMap.values()) {
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
    public ByteBuffer readEntryBuffer(long position, int length) throws IOException{

        return store.readByteBuffer(position, length);
    }


    @Override
    public long appendEntryBuffer(ByteBuffer byteBuffer) throws IOException {
        long start = store.right();
        try {
            byteBuffer.mark();
            // 写入消息
            long position = store.appendByteBuffer(byteBuffer);

            byteBuffer.reset();
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
                                    ThreadSafeFormat.formatWithComma(partition.store.right() / IndexItem.STORAGE_SIZE),
                                    ThreadSafeFormat.formatWithComma(indexItem.getIndex()),
                                    ThreadSafeFormat.formatWithComma(start + byteBuffer.position()),
                                    MessageParser.getString(byteBuffer)));
                }

                if (BatchMessageParser.isBatch(byteBuffer)) {
                    short batchSize = BatchMessageParser.getBatchSize(byteBuffer);
                    indexItem.setBatchMessage(true);
                    indexItem.setBatchMessageSize(batchSize);
                }

                writeIndex(indexItem, partition.store);
                byteBuffer.position(byteBuffer.position() + indexItem.getLength());
            }
            return position;
        } catch (Throwable t) {
            logger.warn("Write failed, rollback to position: {}, topic={}, partitionGroup={}.", start, topic, partitionGroup, t);
            try {
                setRightPosition(start, config.writeTimeoutMs);
            } catch (Throwable e) {
                logger.warn("Rollback failed, rollback to position: {}, topic={}, partitionGroup={}.", start, topic, partitionGroup, e);
            }
            throw t;
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
     *
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
            try  {
                ByteBuffer log = store.read(position);
                if(log != null) {
                    int logTerm = MessageParser.getInt(log, MessageParser.TERM);
                    if (logTerm >= 0) term = logTerm;
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
        if(null != store) store.close();
        for(Partition partition: partitionMap.values()) {
            partition.store.close();
        }
    }

    /**
     * 根据消息存储时间获取索引。
     * 如果找到，返回最后一条 “存储时间 <= timestamp” 消息的索引。
     * 如果找不到，返回负值。
     *
     */
    public long getIndex(short partition, long timestamp) {

        try {
            if(partitionMap.containsKey(partition)) {
                PositioningStore<IndexItem> indexStore  = partitionMap.get(partition).store;
                long searchedIndex =  binarySearchByTimestamp(timestamp, store, indexStore, indexStore.left() / IndexItem.STORAGE_SIZE, indexStore.right() / IndexItem.STORAGE_SIZE  - 1 );

                // 考虑到有可能出现连续n条消息时间相同，找到这n条消息的第一条
                while (searchedIndex -1 >= indexStore.left() && timestamp <= getStorageTimestamp(store, indexStore, searchedIndex - 1 )){
                    searchedIndex --;
                }
                return  searchedIndex;

            }
        }catch (PositionOverflowException | PositionUnderflowException | IOException e) {
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

        if(rightIndexInclude <= leftIndexInclude ) {
            return  -1L;
        }

        if(timestamp <= getStorageTimestamp(journalStore, indexStore, leftIndexInclude)) {
            return leftIndexInclude;
        }

        if(timestamp > getStorageTimestamp(journalStore, indexStore, rightIndexInclude)) {
            return -1;
        }

        if(leftIndexInclude + 1 == rightIndexInclude) {
            return rightIndexInclude;
        }

        long mid = leftIndexInclude + (rightIndexInclude - leftIndexInclude) / 2;

        long midTimestamp = getStorageTimestamp(journalStore, indexStore, mid);

        if(timestamp < midTimestamp) {
            return binarySearchByTimestamp(timestamp,journalStore,indexStore,leftIndexInclude, mid);
        } else {
            return binarySearchByTimestamp(timestamp,journalStore,indexStore,mid, rightIndexInclude);
        }
    }

    private static class Callback {
        Callback(QosLevel qosLevel, EventListener<WriteResult> listener, long[] indices) {
            this.listener = listener;
            this.indices = indices;
            this.qosLevel = qosLevel;
            this.timestamp = System.currentTimeMillis();
        }
        long position;
        EventListener<WriteResult> listener;
        long[] indices;
        long timestamp;
        QosLevel qosLevel;
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
                if(null != indexItem) {
                    if(indexItem.getOffset() + indexItem.getLength() <=  messagePosition) break;
                } else {
                    throw new RollBackException(String.format("Failed to rollback store %s to position %d, batchRead index failed!", store.base().getAbsolutePath(), messagePosition));
                }
                indexPosition -= IndexItem.STORAGE_SIZE;
            }

            store.setRight(indexPosition <= store.left()? 0L : indexPosition + IndexItem.STORAGE_SIZE);

        }

    }

    private static class WriteCommand {
        private final QosLevel qosLevel;
        private final EventListener<WriteResult> eventListener;
        private final ByteBuffer [] messages;
        private WriteCommand(QosLevel qosLevel, EventListener<WriteResult> eventListener, ByteBuffer[] messages) {
            this.qosLevel = qosLevel;
            this.eventListener = eventListener;
            this.messages = messages;
        }
    }

    QosStore getQosStore(QosLevel level) {
        return qosStores[level.value()];
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

        boolean remove(Callback callback) { return queue.remove(callback);}
        void addLast(Callback callback) {
            queue.add(callback);
        }
        /**
         * NOT Thread-safe!!!!!!
         */
        void callbackBefore(long position) {
            callbackPosition.set(position);
            try {
                while (getFirst().position <= position){
                    Callback callback = removeFirst();
                    callback.listener.onEvent(new WriteResult(JMQCode.SUCCESS,callback.indices));
                }
                long deadline = System.currentTimeMillis() - EVENT_TIMEOUT_MILLS;
                while (getFirst().timestamp < deadline) {
                    Callback callback = removeFirst();
                    callback.listener.onEvent(new WriteResult(JMQCode.SE_WRITE_TIMEOUT, null));
                }
            } catch (NoSuchElementException ignored) {}
        }

        void put(Callback callback) {
            addLast(callback);
            if(callback.position <= callbackPosition.get() && remove(callback)){
                callback.listener.onEvent(new WriteResult(JMQCode.SUCCESS, callback.indices));
            }
        }
    }


    public static class Config {
        public static final int DEFAULT_MAX_MESSAGE_LENGTH = 4 * 1024 * 1024;
        public static final long DEFAULT_MAX_STORE_SIZE = 10L * 1024 * 1024 * 1024;
        public static final long DEFAULT_MAX_STORE_TIME = 1000 * 60 * 60 * 24 * 7;
        public static final int DEFAULT_WRITE_REQUEST_CACHE_SIZE = 128;
        public static final long DEFAULT_FLUSH_INTERVAL_MS = 50L;
        public static final long DEFAULT_EVICT_INTERVAL_MS = 100L;
        public static final long DEFAULT_WRITE_TIMEOUT_MS = 5000L;
        public static final long DEFAULT_MAX_DIRTY_SIZE = 10L * 1024 * 1024;
        public static final int DEFAULT_INDEX_BUFFER_LENGTH = 8196;
        public static final int DEFAULT_MESSAGE_BUFFER_LENGTH = 2 * DEFAULT_MAX_MESSAGE_LENGTH;

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
         * 存储上限，超过上限后，最旧的文件将被删除
         */
        private final long maxStoreSize;

        /**
         * 写入请求缓存的大小
         */
        private final int writeRequestCacheSize;

        /**
         * 异步刷盘的时间间隔(ms)
         */
        private final long flushIntervalMs;

        /**
         * 定期清理缓存时间间隔(ms)
         */
        private final long evictIntervalMs;

        private PositioningStore.Config storeConfig;
        private PositioningStore.Config indexStoreConfig;

        public Config() {

            this(DEFAULT_MAX_MESSAGE_LENGTH, DEFAULT_MAX_STORE_SIZE,
                    DEFAULT_WRITE_REQUEST_CACHE_SIZE, DEFAULT_FLUSH_INTERVAL_MS,
                    DEFAULT_WRITE_TIMEOUT_MS, DEFAULT_MAX_DIRTY_SIZE, DEFAULT_EVICT_INTERVAL_MS,
                    new PositioningStore.Config(PositioningStore.Config.DEFAULT_FILE_DATA_SIZE, DEFAULT_MESSAGE_BUFFER_LENGTH),
                    new PositioningStore.Config(PositioningStore.Config.DEFAULT_FILE_DATA_SIZE, DEFAULT_INDEX_BUFFER_LENGTH));
        }

        public Config(int maxMessageLength, long maxStoreSize,
                      int writeRequestCacheSize, long flushIntervalMs,
                      long writeTimeoutMs, long maxDirtySize, long evictIntervalMs,
                      PositioningStore.Config storeConfig,
                      PositioningStore.Config indexStoreConfig) {
            this.maxMessageLength = maxMessageLength;
            this.maxStoreSize = maxStoreSize;
            this.writeRequestCacheSize = writeRequestCacheSize;
            this.flushIntervalMs = flushIntervalMs;
            this.writeTimeoutMs = writeTimeoutMs;
            this.maxDirtySize = maxDirtySize;
            this.evictIntervalMs = evictIntervalMs;
            this.storeConfig = storeConfig;
            this.indexStoreConfig = indexStoreConfig;
        }
    }

}
