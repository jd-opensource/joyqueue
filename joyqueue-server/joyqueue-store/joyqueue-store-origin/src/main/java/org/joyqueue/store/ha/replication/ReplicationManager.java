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
package org.joyqueue.store.ha.replication;

import com.google.common.base.Preconditions;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.config.BrokerConfig;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.monitor.BrokerMonitor;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.store.Store;
import org.joyqueue.store.ha.ReplicableStore;
import org.joyqueue.store.ha.election.*;
import org.joyqueue.store.network.RaftClientTransportFactory;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.lang.Close;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/26
 */
public class ReplicationManager extends Service {
    private static Logger logger = LoggerFactory.getLogger(ReplicationManager.class);

    private ConcurrentHashMap<TopicPartitionGroup, ReplicaGroup> replicaGroups;
    private ElectionConfig electionConfig;
    private BrokerConfig brokerConfig;
    private final ConcurrentHashMap<String, Transport> sessions = new ConcurrentHashMap<>();
    private Store store;
    private Consume consume;
    private TransportClient transportClient;
    private ExecutorService replicateExecutor;
    private ScheduledExecutorService replicateTimerExecutor;
    private BlockingDeque replicateQueue;
    private BrokerContext brokerContext;
    private ElectionService electionService;
    public ReplicationManager(ElectionConfig electionConfig, BrokerConfig brokerConfig, BrokerContext brokerContext, ElectionService electionService, Store store,
                              Consume consume, BrokerMonitor brokerMonitor) {
        Preconditions.checkArgument(electionConfig != null, "election config is null");
        Preconditions.checkArgument(store != null, "store is null");
        Preconditions.checkArgument(consume != null, "consume is null");

        this.electionConfig = electionConfig;
        this.brokerConfig = brokerConfig;
        this.brokerContext=brokerContext;
        this.electionService=electionService;
        this.store = store;
        this.consume = consume;
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();

        replicaGroups = new ConcurrentHashMap<>();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setIoThreadName("joyqueue-Replication-IO-EventLoop");
        clientConfig.setMaxAsync(1000);
        clientConfig.setIoThread(32);
        clientConfig.setSocketBufferSize(1024 * 1024 * 1);
        clientConfig.setConnectionTimeout(300 * 1);
        clientConfig.getRetryPolicy().setRetryDelay(1000 * 60);
        transportClient = new RaftClientTransportFactory(brokerContext,electionService).create(clientConfig);
        transportClient.start();

        replicateQueue = new LinkedBlockingDeque<>(electionConfig.getCommandQueueSize());
        replicateExecutor = new ThreadPoolExecutor(electionConfig.getReplicateThreadNumMin(), electionConfig.getReplicateThreadNumMax(),
                60, TimeUnit.SECONDS, replicateQueue,
                new NamedThreadFactory("Replicate-sendCommand"));

        replicateTimerExecutor = Executors.newScheduledThreadPool(electionConfig.getTimerScheduleThreadNum());

        replicateTimerExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    ConcurrentHashMap<TopicPartitionGroup, ReplicaGroup> replicaGroups = ReplicationManager.this.replicaGroups;
                    int replicaGroupCount = 0;
                    int replicaLeaderCount = 0;
                    for (ReplicaGroup replicaGroup : replicaGroups.values()) {
                        replicaGroupCount++;
                        if (replicaGroup.isLeader()) {
                            replicaLeaderCount++;
                        }
                    }
                    logger.info("ReplicationManager, managed replica group count {} ,leader count {} , replicate queue capacity is {}, current size is {}",
                            replicaGroupCount, replicaLeaderCount, electionConfig.getCommandQueueSize(), replicateQueue.size());
                } catch (Throwable th) {
                    logger.warn("ReplicateManger schedule error.", th);
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void doStop() {
        Close.close(transportClient);
        Close.close(replicateExecutor);

        super.doStop();
    }

    /**
     *  Create and start replica group
     *
     *
     **/
    public synchronized ReplicaGroup createReplicaGroup(String topic, int partitionGroup, List<DefaultElectionNode> allNodes,
                                                        Set<Integer> learners, int localReplicaId, int leaderId, BrokerMonitor brokerMonitor) throws ElectionException {
        TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup(topic, partitionGroup);
        ReplicaGroup replicaGroup = replicaGroups.get(topicPartitionGroup);
        if (replicaGroup != null) {
            logger.warn("Create replica group for topic {} partition group {} failed, " +
                    "replication group is not null", topic, partitionGroup);
            removeReplicaGroup(topic, partitionGroup);
        }

        ReplicableStore replicableStore =store.replicableStore(topic,partitionGroup) ;//storeService.getReplicableStore(topic, partitionGroup);
        if (replicableStore == null) {
            logger.info("Create replica group for topic {} partition group {} failed, " +
                    "replicable store is null", topic, partitionGroup);
            throw new ElectionException(String.format("Create Replica group for topic %s partition group " +
                    "%d failed, replicable store is null", topic, partitionGroup));
        }
        replicaGroup = new ReplicaGroup(topicPartitionGroup, this, replicableStore, electionConfig, brokerConfig,
                consume, replicateExecutor, brokerMonitor, allNodes, learners, localReplicaId, leaderId, transportClient);
        try {
            replicaGroup.start();
        } catch (Exception e) {
            throw new ElectionException("Create replica group fail" + e);
        }
        replicaGroups.put(topicPartitionGroup, replicaGroup);

        return replicaGroup;
    }

    public synchronized void removeReplicaGroup(String topic, int partitionGroup) {
        TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup(topic, partitionGroup);
        ReplicaGroup replicaGroup = replicaGroups.get(topicPartitionGroup);
        if (replicaGroup == null) {
            logger.info("Remove replica group of topic {} partition group {}, " +
                    "replication group is null", topic, partitionGroup);
            return;
        }

        replicaGroup.stop();
        replicaGroups.remove(topicPartitionGroup);
    }

    public ReplicaGroup getReplicaGroup(String topic, int partitionGroup) {
        ReplicaGroup replicaGroup = replicaGroups.get(new TopicPartitionGroup(topic, partitionGroup));
        if (replicaGroup == null) {
            logger.info("Get replica group of topic {} partition group {}, " +
                    "replication group is null", topic, partitionGroup);
        }
        return replicaGroup;
    }
}
