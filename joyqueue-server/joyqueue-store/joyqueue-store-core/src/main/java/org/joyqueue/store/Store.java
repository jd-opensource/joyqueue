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
import org.joyqueue.monitor.BufferPoolMonitorInfo;
import org.joyqueue.store.event.StoreEvent;
import org.joyqueue.store.file.PositioningStore;
import org.joyqueue.store.index.IndexItem;
import org.joyqueue.store.replication.ReplicableStore;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.store.transaction.TransactionStoreManager;
import org.joyqueue.store.utils.PreloadBufferPool;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author liyue25
 * Date: 2018/8/13
 * <p>
 * root                            # 数据文件根目录
 * ├── lock                        # 进程锁目录，避免多进程同时操作导致数据损坏
 * └── topics                      # 所有topic目录，子目录就是topic名称
 *     ├── coupon                  # topic coupon
 *     └── order                   # topic order
 *         └── 1                   # partition group 1
 */
public class Store extends Service implements StoreService, Closeable, PropertySupplierAware {

    private static final Logger logger = LoggerFactory.getLogger(Store.class);
    // 所有topic目录，子目录就是topic名称
    private static final String TOPICS_DIR = "topics";
    private static final String TX_DIR = "tx";
    private static final String DEL_PREFIX = ".d.";

    private final Map<String /* Partition Group，格式为：[topic]/[group index] */, PartitionGroupStoreManager> storeMap = new HashMap<>();
    private final Map<String, TransactionStoreManager> txStoreMap = new HashMap<>();
    private StoreConfig config;
    private PreloadBufferPool bufferPool;
    private File base;
    private PropertySupplier propertySupplier;
    // 文件锁，防止同一Store目录被多个进程读写
    private StoreLock storeLock;

    public Store() {
        //do nothing
    }

    public Store(StoreConfig config) {
        this.config = config;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (config == null) {
            config = new StoreConfig(propertySupplier);
        }
        if (base == null) {
            base = new File(config.getPath());
        }
        checkOrCreateBase();
        if (storeLock == null) {
            storeLock = new StoreLock(new File(base, "lock"));
            storeLock.lock();
        }

        // 初始化文件缓存页
        if (bufferPool == null) {
            System.setProperty(PreloadBufferPool.PRINT_METRIC_INTERVAL_MS_KEY, String.valueOf(config.getPrintMetricIntervalMs()));
            this.bufferPool = PreloadBufferPool.getInstance();
        }
        this.bufferPool.addPreLoad(config.getIndexFileSize(), config.getPreLoadBufferCoreCount(), config.getPreLoadBufferMaxCount());
        this.bufferPool.addPreLoad(config.getMessageFileSize(), config.getPreLoadBufferCoreCount(), config.getPreLoadBufferMaxCount());

    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        logger.info("Starting store {}...", base.getPath());

        for (PartitionGroupStoreManager manger : storeMap.values()) {
            if (!manger.isStarted()) manger.start();
        }

        started.set(true); // FixMe: 这条语句是否应该删除？
        logger.info("Store started.");
    }

    @Override
    protected void doStop() {
        super.doStop();

        logger.info("Stopping store {}...", base.getPath());

        storeMap.values().forEach(p -> {
            p.disable();
            p.stop();
        });

        storeLock.unlock();

        logger.info("Store {} stopped.", base.getPath());
    }

    public void checkOrCreateBase() {
        if (!base.exists()) {
            if (!base.mkdirs()) throw new StoreInitializeException(String.format("Failed to create directory: %s.", base.getAbsolutePath()));
        } else {
            if (!base.isDirectory()) throw new StoreInitializeException(String.format("Failed to create directory: %s! Cause: file exists!", base.getAbsolutePath()));
        }
    }


    public boolean physicalDelete() {
        if (started.get()) {
            logger.info("Stop me fist!");
            return false;
        } else {
            logger.info("PHYSICAL DELETE {}...", base.getAbsolutePath());
            deleteFolder(base);
            return true;
        }
    }

    @Override
    public boolean partitionGroupExists(String topic, int partitionGroup) {
        return new File(base, getPartitionGroupRelPath(topic, partitionGroup)).isDirectory();
    }

    @Override
    public boolean topicExists(String topic) {
        return new File(base, getTopicRelPath(topic)).isDirectory();
    }

    @Override
    public TransactionStore getTransactionStore(String topic) {
        synchronized (txStoreMap) {
            if (txStoreMap.containsKey(topic)) {
                return txStoreMap.get(topic);
            } else {
                File txBase = new File(new File(base, getTopicRelPath(topic)), TX_DIR);
                if (topicExists(topic) && (txBase.isDirectory() || txBase.mkdirs())) {
                    TransactionStoreManager transactionStore =
                            new TransactionStoreManager(txBase,
                                    getMessageStoreConfig(config), bufferPool);
                    txStoreMap.put(topic, transactionStore);
                    return transactionStore;
                } else {
                    return null;
                }
            }
        }


    }

    @Override
    public List<TransactionStore> getAllTransactionStores() {
        return this.storeMap.keySet().stream()
                .map(key -> key.replaceAll("^(.*)/\\d+$", "$1"))
                .distinct()
                .map(topic -> {
                    synchronized (txStoreMap) {
                        if (txStoreMap.containsKey(topic)) {
                            return txStoreMap.get(topic);
                        } else {
                            File txBase = new File(new File(base, getTopicRelPath(topic)), TX_DIR);
                            if (txBase.isDirectory()) {
                                TransactionStoreManager transactionStore = new TransactionStoreManager(txBase,
                                        getMessageStoreConfig(config), bufferPool);
                                txStoreMap.put(topic, transactionStore);
                                return transactionStore;
                            } else {
                                return null;
                            }
                        }
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public synchronized void removePartitionGroup(String topic, int partitionGroup) {
        PartitionGroupStoreManager partitionGroupStoreManger = storeMap.remove(topic + "/" + partitionGroup);
        if (null != partitionGroupStoreManger) {
            partitionGroupStoreManger.stop();
            partitionGroupStoreManger.close();
        }
        File groupBase = new File(base, getPartitionGroupRelPath(topic, partitionGroup));

        if (groupBase.exists()) delete(groupBase);


        File topicBase = new File(base, getTopicRelPath(topic));

        File[] files = topicBase.listFiles((dir, name) -> name.matches("\\d+"));
        if (null == files || files.length == 0) {
            synchronized (txStoreMap) {
                if (txStoreMap.containsKey(topic)) {
                    TransactionStoreManager transactionStore = txStoreMap.remove(topic);
                    if (null != transactionStore) {
                        transactionStore.close();
                    }
                }
            }
            delete(topicBase);
        }

    }


    @Override
    public synchronized void restorePartitionGroup(String topic, int partitionGroup) throws Exception {

        PartitionGroupStoreManager partitionGroupStoreManger = partitionGroupStore(topic, partitionGroup);
        if (null == partitionGroupStoreManger) {
            File groupBase = new File(base, getPartitionGroupRelPath(topic, partitionGroup));
            partitionGroupStoreManger = new PartitionGroupStoreManager(topic, partitionGroup, groupBase
                    , getPartitionGroupConfig(config)
                    , bufferPool);
            partitionGroupStoreManger.recover();
            if (isStarted()) {
                partitionGroupStoreManger.start();
            }
            storeMap.put(topic + "/" + partitionGroup, partitionGroupStoreManger);

        }
    }

    @Override
    public synchronized void createPartitionGroup(String topic, int partitionGroup, short[] partitions) throws Exception {
        if (!storeMap.containsKey(topic + "/" + partitionGroup)) {
            File groupBase = new File(base, getPartitionGroupRelPath(topic, partitionGroup));
            if (groupBase.exists()) delete(groupBase);
            PartitionGroupStoreSupport.init(groupBase, partitions);

            restorePartitionGroup(topic, partitionGroup);
        }
    }


    private PartitionGroupStoreManager.Config getPartitionGroupConfig(StoreConfig config) {

        PositioningStore.Config messageConfig = getMessageStoreConfig(config);
        PositioningStore.Config indexConfig = getIndexStoreConfig(config);
        return new PartitionGroupStoreManager.Config(
                config.getMaxMessageLength(), config.getWriteRequestCacheSize(), config.getFlushIntervalMs(),
                config.getWriteTimeoutMs(), config.getMaxDirtySize(),
                config.getPrintMetricIntervalMs(), messageConfig, indexConfig);
    }

    private PositioningStore.Config getIndexStoreConfig(StoreConfig config) {
        return new PositioningStore.Config(config.getIndexFileSize(),
                config.getFileHeaderSize(), config.getDiskFullRatio(), IndexItem.STORAGE_SIZE, config.isIndexFileLoadOnRead(), config.isFlushForce());
    }

    private PositioningStore.Config getMessageStoreConfig(StoreConfig config) {
        return new PositioningStore.Config(config.getMessageFileSize(),
                config.getFileHeaderSize(), config.getDiskFullRatio(),config.getMaxMessageLength(), config.isMessageFileLoadOnRead(), config.isFlushForce());
    }

    /**
     * 并不真正删除，只是重命名
     */
    private boolean delete(File file) {
        File renamed = new File(file.getParent(), DEL_PREFIX + SystemClock.now() + "." + file.getName());
        return file.renameTo(renamed);
    }

    @Override
    public PartitionGroupStore getStore(String topic, int partitionGroup, QosLevel writeQosLevel) {
        PartitionGroupStoreManager partitionGroupStoreManager = partitionGroupStore(topic, partitionGroup);
        return partitionGroupStoreManager == null ? null : partitionGroupStoreManager.getQosStore(writeQosLevel);
    }

    @Override
    public PartitionGroupStore getStore(String topic, int partitionGroup) {
        return getStore(topic, partitionGroup, QosLevel.REPLICATION);
    }

    @Override
    public List<PartitionGroupStore> getStore(String topic) {
        return partitionGroupStores(topic).stream().map(p -> p.getQosStore(QosLevel.REPLICATION)).collect(Collectors.toList());
    }

    @Override
    public void rePartition(String topic, int partitionGroup, Short[] partitions) throws IOException {
        PartitionGroupStoreManager partitionGroupStoreManger = partitionGroupStore(topic, partitionGroup);
        if (null != partitionGroupStoreManger) {
            partitionGroupStoreManger.rePartition(partitions);
        } else {
            throw new NoSuchPartitionGroupException();
        }
    }

    /**
     * 获取用于选举复制的存储
     */
    @Override
    public ReplicableStore getReplicableStore(String topic, int partitionGroup) {
        return partitionGroupStore(topic, partitionGroup);
    }

    /**
     * 获取管理接口
     */
    @Override
    public StoreManagementService getManageService() {

        return new StoreManagement(128, 128, config.getMaxMessageLength(), bufferPool, this);
    }

    @Override
    public BufferPoolMonitorInfo monitorInfo() {
        return bufferPool.monitorInfo();
    }

    @Override
    public StoreNodes getNodes(String topic, int partitionGroup) {
        PartitionGroupStoreManager partitionGroupStoreManger = partitionGroupStore(topic, partitionGroup);
        if (partitionGroupStoreManger == null) {
            return null;
        }
        return new StoreNodes(new StoreNode(0, true, true));
    }

    @Override
    public void addListener(EventListener<StoreEvent> listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(EventListener<StoreEvent> listener) {
        throw new UnsupportedOperationException();
    }

    private String getPartitionGroupRelPath(String topic, int partitionGroup) {
        return TOPICS_DIR + File.separator + topic.replace('/', '@') + File.separator + partitionGroup;
    }

    private String getTopicRelPath(String topic) {
        return TOPICS_DIR + File.separator + topic;
    }


    File base() {
        return base;
    }


    @Override
    public void close() throws IOException {
        for (PartitionGroupStoreManager p : storeMap.values()) {
            p.close();
        }
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    if (!f.delete()) {
                        logger.warn("Delete failed: {}", f.getAbsolutePath());
                    }
                }
            }
        }
        if (!folder.delete()) {
            logger.warn("Delete failed: {}", folder.getAbsolutePath());

        }
    }


    List<String> topics() {
        return storeMap.keySet().stream().map(k -> k.split("/")[0]).distinct().collect(Collectors.toList());
    }

    List<String> partitionGroups() {
        return new ArrayList<>(storeMap.keySet());
    }

    List<Integer> partitionGroups(String topic) {
        return storeMap.keySet().stream()
                .filter(k -> k.matches("^" + topic + "/\\d+$"))
                .map(k -> k.substring(topic.length() + 1))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
    PartitionGroupStoreManager partitionGroupStore(String topic, int partitionGroup) {
        return storeMap.get(topic + "/" + partitionGroup);
    }

    List<PartitionGroupStoreManager> partitionGroupStores(String topic) {
        return partitionGroups(topic).stream().map(id -> storeMap.get(topic + "/" + id)).collect(Collectors.toList());
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
        if (config == null) {
            config = new StoreConfig(supplier);
        }
    }
}
