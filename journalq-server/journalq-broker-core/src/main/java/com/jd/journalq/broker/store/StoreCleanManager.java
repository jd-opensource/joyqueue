package com.jd.journalq.broker.store;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.position.PositionManager;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.concurrent.NamedThreadFactory;
import com.jd.journalq.toolkit.config.PropertySupplier;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import com.jd.laf.extension.ExtensionManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author majun8
 */
public class StoreCleanManager extends Service {
    private static final Logger LOG = LoggerFactory.getLogger(StoreCleanManager.class);

    private static final int SCHEDULE_EXECUTOR_THREADS = 16;
    private PropertySupplier propertySupplier;
    private StoreService storeService;
    private ClusterManager clusterManager;
    private PositionManager positionManager;
    private List<StoreCleaningStrategy> storeCleaningStrategies;
    private final ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture cleanFuture;

    public StoreCleanManager(PropertySupplier propertySupplier, StoreService storeService, ClusterManager clusterManager, PositionManager positionManager) {
        this.propertySupplier = propertySupplier;
        this.storeService = storeService;
        this.clusterManager = clusterManager;
        this.positionManager = positionManager;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(SCHEDULE_EXECUTOR_THREADS, new NamedThreadFactory("StoreCleaning-Scheduled-Executor"));
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        Preconditions.checkArgument(propertySupplier != null, "property supplier can not be null");
        Preconditions.checkArgument(storeService != null, "store service can not be null");
        Preconditions.checkArgument(positionManager != null, "position manager can not be null");

        storeCleaningStrategies = initStoreCleaningStrategyList();
        Preconditions.checkArgument(storeCleaningStrategies.size() != 0, "cleaning strategy can not be null");
        for (StoreCleaningStrategy cleaningStrategy : storeCleaningStrategies) {
            cleaningStrategy.setSupplier(propertySupplier);
        }
    }

    private List<StoreCleaningStrategy> initStoreCleaningStrategyList() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(StoreCleaningStrategy.class));
    }

    @Override
    public void start() throws Exception {
        super.start();
        cleanFuture = scheduledExecutorService.scheduleWithFixedDelay(this::clean,
                ThreadLocalRandom.current().nextLong(500L, 1000L),
                ThreadLocalRandom.current().nextLong(500L, 1000L),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        super.stop();
        try {
            long stopTimeout = 5000L;
            if (cleanFuture != null) {
                long t0 = System.currentTimeMillis();
                while (!cleanFuture.isDone()) {
                    if (System.currentTimeMillis() - t0 > stopTimeout) {
                        throw new TimeoutException("Wait for async store clean job timeout!");
                    }
                    cleanFuture.cancel(true);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        LOG.warn("Exception: ", e);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void clean() {
        Map<TopicConfig, List<TopicPartitionAckIndex>> topicConfigListMap = new HashMap<>();
        List<TopicConfig> topicConfigs = clusterManager.getTopics();
        if (topicConfigs != null && topicConfigs.size() > 0) {
            topicConfigs.forEach(
                    topicConfig -> {
                        List<PartitionGroup> partitionGroups = clusterManager.getTopicPartitionGroups(topicConfig.getName());
                        if (CollectionUtils.isNotEmpty(partitionGroups)) {
                            partitionGroups.forEach(
                                    partitionGroup -> {
                                        Set<Short> partitions = partitionGroup.getPartitions();
                                        if (CollectionUtils.isNotEmpty(partitions)) {
                                            List<String> appList = clusterManager.getAppByTopic(topicConfig.getName());
                                            List<TopicPartitionAckIndex> ackIndices = new ArrayList<>(partitions.size());
                                            partitions.forEach(
                                                    partition -> {
                                                        long minAckIndex = Long.MAX_VALUE;
                                                        if (CollectionUtils.isNotEmpty(appList)) {
                                                            for (String app : appList) {
                                                                try {
                                                                    minAckIndex = Math.min(minAckIndex, positionManager.getLastMsgAckIndex(topicConfig.getName(), app, partition));
                                                                } catch (JMQException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                        ackIndices.add(new TopicPartitionAckIndex(topicConfig.getName(), partition, minAckIndex));
                                                    }
                                            );
                                            topicConfigListMap.put(topicConfig, ackIndices);
                                        }
                                    }
                            );
                        }
                    }
            );
        }

        // Partition PartitionGroup

        if (!topicConfigListMap.isEmpty()) {
            topicConfigListMap.forEach(
                    (topicConfig, ackIndices) -> {
                        long minPartitionAckIndex = Long.MAX_VALUE;
                        for (TopicPartitionAckIndex partitionAckIndex : ackIndices) {
                            minPartitionAckIndex = Math.min(minPartitionAckIndex, partitionAckIndex.getMinAckIndex());
                        }
                        try {
                            for (StoreCleaningStrategy cleaningStrategy : storeCleaningStrategies) {
                                cleaningStrategy.deleteIfNeeded(storeService, topicConfig.getName(), minPartitionAckIndex);
                            }
                        } catch (IOException e) {
                            LOG.error("Delete message storage error: <{}>, <{}>", ackIndices, e);
                            e.printStackTrace();
                        }
                    }
            );
        }
    }

    public class TopicPartitionAckIndex {
        private TopicName topicName;
        private Short partition;
        private long minAckIndex;

        public TopicPartitionAckIndex(TopicName topicName, Short partition, long minAckIndex) {
            this.topicName = topicName;
            this.partition = partition;
            this.minAckIndex = minAckIndex;
        }

        public TopicName getTopicName() {
            return topicName;
        }

        public void setTopicName(TopicName topicName) {
            this.topicName = topicName;
        }

        public Short getPartition() {
            return partition;
        }

        public void setPartition(Short partition) {
            this.partition = partition;
        }

        public long getMinAckIndex() {
            return minAckIndex;
        }

        public void setMinAckIndex(long minAckIndex) {
            this.minAckIndex = minAckIndex;
        }

        @Override
        public String toString() {
            return "TopicPartitionAckIndex{" +
                    "topicName=" + topicName +
                    ", partition=" + partition +
                    ", minAckIndex=" + minAckIndex +
                    '}';
        }
    }
}
