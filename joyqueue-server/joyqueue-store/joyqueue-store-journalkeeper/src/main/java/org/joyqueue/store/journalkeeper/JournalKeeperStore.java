package org.joyqueue.store.journalkeeper;

import io.journalkeeper.core.api.RaftServer;
import io.journalkeeper.core.strategy.JournalCompactionStrategy;
import io.journalkeeper.rpc.URIParser;
import io.journalkeeper.utils.spi.ServiceSupport;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.monitor.BufferPoolMonitorInfo;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreManagementService;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @author LiYue
 * Date: 2019-09-19
 */
public class JournalKeeperStore extends Service implements StoreService, PropertySupplierAware, BrokerContextAware {
    private static final Logger logger = LoggerFactory.getLogger(JournalKeeperStore.class);
    private static final String TOPICS_PATH = "topics";
    private static final String STORE_PATH = "store";
    private Map<TopicPartitionGroup, JournalKeeperPartitionGroupStore> storeMap = new ConcurrentHashMap<>();
    private File base;
    private BrokerContext brokerContext;
    private final ManagementServiceImpl managementService = new ManagementServiceImpl(Collections.unmodifiableMap(storeMap));
    private ExecutorService asyncExecutor;
    private ScheduledExecutorService scheduledExecutor;
    private final JoyQueueUriParser joyQueueUriParser =
            ServiceSupport.load(URIParser.class, JoyQueueUriParser.class.getCanonicalName());
    private final JoyQueueJournalCompactionStrategy journalCompactionStrategy =
            ServiceSupport.load(JournalCompactionStrategy.class, JoyQueueJournalCompactionStrategy.class.getCanonicalName());
    private static final int SCHEDULE_EXECUTOR_THREADS = 32;
    @Override
    protected void doStop() {
        super.doStop();
        for (JournalKeeperPartitionGroupStore store : storeMap.values()) {
            store.stop();
        }
        if(null != asyncExecutor) {
            asyncExecutor.shutdown();
        }
        if(null != scheduledExecutor) {
            scheduledExecutor.shutdown();
        }
    }


    @Override
    protected void doStart() throws Exception {
        super.doStart();
        scheduledExecutor = Executors.newScheduledThreadPool(SCHEDULE_EXECUTOR_THREADS, new NamedThreadFactory("Store-Scheduled-Executor"));
        asyncExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("Store-Async-Executor"));
    }

    @Override
    public TransactionStore getTransactionStore(String topic, short partition) {
        JournalKeeperPartitionGroupStore store = getStore(topic, partition);

        return store == null ? null : store.getTransactionStore();
    }

    @Override
    public List<TransactionStore> getAllTransactionStores() {
        return storeMap.values().stream()
                .map(JournalKeeperPartitionGroupStore::getTransactionStore)
                .collect(Collectors.toList());
    }

    @Override
    public void removePartitionGroup(String topic, int partitionGroup) {
        JournalKeeperPartitionGroupStore store = storeMap.remove(new TopicPartitionGroup(topic, partitionGroup));
        if(null != store) {
            store.stop();
            File groupBase = new File(base, getPartitionGroupRelPath(topic, partitionGroup));
            deleteDirectoryRecursively(groupBase);
        } else {
            logger.warn("Remove partition group failed, partition group not exist! Topic: {}, partitionGroup: {}.",
                    topic, partitionGroup);
        }
    }

    private static void deleteDirectoryRecursively(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectoryRecursively(f);
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

    @Override
    public PartitionGroupStore restoreOrCreatePartitionGroup(String topic, int partitionGroup, short[] partitions, List<Integer> brokerIds, int thisBrokerId) {
        return storeMap.computeIfAbsent(new TopicPartitionGroup(topic, partitionGroup), tg -> {
            try {
                JournalKeeperPartitionGroupStore store =
                        new JournalKeeperPartitionGroupStore(
                                topic,
                                partitionGroup,
                                RaftServer.Roll.VOTER,
                                new LeaderReportEventWatcher(topic, partitionGroup, brokerContext.getClusterManager()),
                                asyncExecutor, scheduledExecutor, partitionGroupProperties(tg));
                if(!store.isInitialized()) {
                    store.init(toURIs(brokerIds, topic, partitionGroup), toURI(thisBrokerId, topic, partitionGroup), partitions);
                }
                store.restore();
                store.start();
                return store;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    private Properties partitionGroupProperties(TopicPartitionGroup tg) {
        File groupBase = new File(base, getPartitionGroupRelPath(tg.getTopic(), tg.getPartitionGroup()));
        Properties properties = new Properties();
        properties.setProperty("working_dir", groupBase.getAbsolutePath());
        properties.setProperty("disable_logo", "true");
        //TODO: StoreConfig -> properties

        return properties;
    }

    @Override
    public PartitionGroupStore createPartitionGroup(String topic, int partitionGroup, short[] partitions, List<Integer> brokerIds, int thisBrokerId) {
        return storeMap.computeIfAbsent(new TopicPartitionGroup(topic, partitionGroup), tg -> {
            try {
                JournalKeeperPartitionGroupStore store =
                        new JournalKeeperPartitionGroupStore(
                                topic,
                                partitionGroup,
                                RaftServer.Roll.VOTER,
                                new LeaderReportEventWatcher(topic, partitionGroup, brokerContext.getClusterManager()),
                                asyncExecutor, scheduledExecutor, partitionGroupProperties(tg));
                store.init(toURIs(brokerIds, topic, partitionGroup), toURI(thisBrokerId, topic, partitionGroup), partitions);
                store.restore();
                store.start();
                return store;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void stopPartitionGroup(String topic, int partitionGroup) {
        JournalKeeperPartitionGroupStore store = storeMap.get(new TopicPartitionGroup(topic, partitionGroup));
        if(null != store) {
            store.stop();
            logger.warn("Partition group stopped. " +
                    "Topic: {}, partition group: {}.", topic, partitionGroup);
        } else {
            logger.warn("Stop partition group failed! Cause: no such partition group. " +
                    "Topic: {}, partition group: {}.", topic, partitionGroup);
        }
    }

    @Override
    public PartitionGroupStore getStore(String topic, int partitionGroup) {
        return storeMap.get(new TopicPartitionGroup(topic, partitionGroup));
    }

    @Override
    public Collection<PartitionGroupStore> getAllStores() {
        return new ArrayList<>(storeMap.values());
    }

    private JournalKeeperPartitionGroupStore getStore(String topic, short partition) {
        return storeMap
                .entrySet().stream()
                .filter(entry-> topic.equals(entry.getKey().getTopic()))
                .map(Map.Entry::getValue)
                .filter(store -> Arrays.stream(store.listPartitions()).anyMatch(p -> p == partition))
                .findAny().orElse(null);
    }

    @Override
    public List<PartitionGroupStore> getStore(String topic) {
        if(null == topic) {
            throw new IllegalArgumentException("Topic can not be null!");
        }
        return storeMap
                .entrySet().stream()
                .filter(entry-> topic.equals(entry.getKey().getTopic()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public void maybeRePartition(String topic, int partitionGroup, Collection<Short> partitions) {
        JournalKeeperPartitionGroupStore store = storeMap.get(new TopicPartitionGroup(topic, partitionGroup));
        if(null != store) {
            store.maybeRePartition(partitions);
        } else {
            logger.warn("Repartition failed, partition group not exist! Topic: {}, partitionGroup: {}, partitions: {}.",
                    topic, partitionGroup, partitions);
        }
    }

    @Override
    public void maybeUpdateReplicas(String topic, int partitionGroup, Collection<Integer> newReplicaBrokerIds) {
        JournalKeeperPartitionGroupStore store = storeMap.get(new TopicPartitionGroup(topic, partitionGroup));
        if(null != store) {
            store.maybeUpdateConfig(toURIs(new ArrayList<>(newReplicaBrokerIds), topic, partitionGroup));
        } else {
            logger.warn("Update config failed, partition group not exist! Topic: {}, partitionGroup: {}.",
                    topic, partitionGroup);
        }
    }

    @Override
    public StoreManagementService getManageService() {
        return managementService;
    }

    @Override
    public BufferPoolMonitorInfo monitorInfo() {
        // TODO
        return null;
    }

    @Override
    public List<TransactionStore> getTransactionStores(String topic) {
        return null;
    }

    private String getPartitionGroupRelPath(String topic, int partitionGroup) {
        return TOPICS_PATH + File.separator + topic.replace('/', '@') + File.separator + partitionGroup;
    }

    private List<URI> toURIs(List<Integer> brokerIds, String topic, int group) {
        return brokerIds.stream()
                .map(brokerId -> toURI(brokerId, topic, group))
                .collect(Collectors.toList());
    }

    private URI toURI(int brokerId, String topic, int group) {
        return joyQueueUriParser.create(topic, group, brokerId);
    }

    private void checkOrCreateBase() throws IOException{
        if (!base.exists()) {
            if (!base.mkdirs()) {
                throw new IOException(String.format("Failed to create base directory: %s!", base.getAbsolutePath()));
            }
        } else {
            if (!base.isDirectory()) {
                throw new IOException(String.format("File %s is not a directory!", base.getAbsolutePath()));
            }
        }
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        try {
            Property property = supplier.getProperty(Property.APPLICATION_DATA_PATH);
            base = new File(property.getString() + File.separator + STORE_PATH);
            checkOrCreateBase();
        }catch (Exception e) {
            logger.warn("Exception: ", e);
        }
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        joyQueueUriParser.setBrokerContext(brokerContext);
        journalCompactionStrategy.setBrokerContext(brokerContext);
    }

    static class TopicPartitionGroup {
        private final String topic;
        private final int partitionGroup;
        TopicPartitionGroup(String topic, int partitionGroup) {
            this.topic = topic;
            this.partitionGroup = partitionGroup;
        }
        public String getTopic() {
            return topic;
        }

        int getPartitionGroup() {
            return partitionGroup;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TopicPartitionGroup that = (TopicPartitionGroup) o;
            return partitionGroup == that.partitionGroup &&
                    topic.equals(that.topic);
        }

        @Override
        public int hashCode() {
            return Objects.hash(topic, partitionGroup);
        }
    }

    @Override
    public String name() {
        return "JournalKeeper";
    }
}
