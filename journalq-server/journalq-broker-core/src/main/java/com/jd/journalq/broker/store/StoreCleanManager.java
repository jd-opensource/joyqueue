/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.broker.store;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.config.BrokerStoreConfig;
import com.jd.journalq.broker.consumer.position.PositionManager;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.concurrent.NamedThreadFactory;
import com.jd.journalq.toolkit.config.PropertySupplier;
import com.google.common.base.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import com.jd.laf.extension.ExtensionManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author majun8
 */
public class StoreCleanManager extends Service {
    private static final Logger LOG = LoggerFactory.getLogger(StoreCleanManager.class);

    private static final int SCHEDULE_EXECUTOR_THREADS = 16;
    private PropertySupplier propertySupplier;
    private BrokerStoreConfig brokerStoreConfig;
    private StoreService storeService;
    private ClusterManager clusterManager;
    private PositionManager positionManager;
    private Map<String, StoreCleaningStrategy> cleaningStrategyMap;
    private final ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture cleanFuture;

    public StoreCleanManager(PropertySupplier propertySupplier, StoreService storeService, ClusterManager clusterManager, PositionManager positionManager) {
        this.propertySupplier = propertySupplier;
        this.brokerStoreConfig = new BrokerStoreConfig(propertySupplier);
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

        List<StoreCleaningStrategy> storeCleaningStrategies = initStoreCleaningStrategyList();
        Preconditions.checkArgument(storeCleaningStrategies.size() != 0, "load cleaning strategy list can not be null");
        cleaningStrategyMap = new HashMap<>(storeCleaningStrategies.size());
        for (StoreCleaningStrategy cleaningStrategy : storeCleaningStrategies) {
            cleaningStrategy.setSupplier(propertySupplier);
            cleaningStrategyMap.put(cleaningStrategy.getClass().getSimpleName(), cleaningStrategy);
        }
    }

    private List<StoreCleaningStrategy> initStoreCleaningStrategyList() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(StoreCleaningStrategy.class));
    }

    @Override
    public void start() throws Exception {
        super.start();
        cleanFuture = scheduledExecutorService.scheduleWithFixedDelay(this::clean,
                ThreadLocalRandom.current().nextLong(brokerStoreConfig.getStoreCleanScheduleBegin(), brokerStoreConfig.getStoreCleanScheduleEnd()),
                ThreadLocalRandom.current().nextLong(brokerStoreConfig.getStoreCleanScheduleBegin(), brokerStoreConfig.getStoreCleanScheduleEnd()),
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
            LOG.error(t.getMessage(), t);
        }
    }

    private void clean() {
        LOG.info("start scheduled StoreCleaningStrategy task use class: <{}>!!!", brokerStoreConfig.getCleanStrategyClass());
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
                                            Map<Short, Long> partitionAckMap = new HashMap<>(partitions.size());
                                            partitions.forEach(
                                                    partition -> {
                                                        long minAckIndex = Long.MAX_VALUE;
                                                        if (CollectionUtils.isNotEmpty(appList)) {
                                                            for (String app : appList) {
                                                                try {
                                                                    minAckIndex = Math.min(minAckIndex, positionManager.getLastMsgAckIndex(topicConfig.getName(), app, partition));
                                                                } catch (JMQException e) {
                                                                    //minAckIndex = Long.MAX_VALUE;
                                                                    LOG.error("Error to get last topic & app offset, topic <{}>, app <{}>, partitionGroup <{}>, partition <{}>, error: <{}>",
                                                                            topicConfig.getName(), app, partitionGroup.getGroup(), partition, e);
                                                                }
                                                            }
                                                        }
                                                        partitionAckMap.put(partition, minAckIndex);
                                                    }
                                            );
                                            StoreCleaningStrategy cleaningStrategy = null;
                                            try {
                                                cleaningStrategy = cleaningStrategyMap.get(brokerStoreConfig.getCleanStrategyClass());
                                                if (cleaningStrategy != null) {
                                                    LOG.info("Begin store clean topic: <{}>, partition group: <{}>, partition ack map: <{}>",
                                                            topicConfig.getName().getFullName(), partitionGroup.getGroup(), partitionAckMap);
                                                    cleaningStrategy.deleteIfNeeded(storeService.getStore(topicConfig.getName().getFullName(), partitionGroup.getGroup()), partitionAckMap);
                                                }
                                            } catch (IOException e) {
                                                LOG.error("Error to clean store for topic <{}>, partition group <{}>, delete partitions index <{}> on clean class <{}>, exception: <{}>",
                                                        topicConfig, partitionGroup.getGroup(), partitionAckMap, cleaningStrategy, e);
                                            }
                                        }
                                    }
                            );
                        }
                    }
            );
        }
    }
}
