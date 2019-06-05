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
package com.jd.journalq.broker.election;

import com.alibaba.fastjson.JSON;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.config.BrokerConfig;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.monitor.BrokerMonitor;
import com.jd.journalq.broker.network.support.BrokerTransportClientFactory;
import com.jd.journalq.broker.replication.ReplicaGroup;
import com.jd.journalq.broker.replication.ReplicationManager;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.event.TransportEvent;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.TransportAttribute;
import com.jd.journalq.network.transport.TransportClient;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.config.ClientConfig;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.network.transport.support.DefaultTransportAttribute;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.store.replication.ReplicableStore;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.concurrent.NamedThreadFactory;
import com.google.common.base.Preconditions;
import com.jd.journalq.toolkit.lang.Close;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 管理选举动作，每个PartitionGroup作为一个选举集群
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class ElectionManager extends Service implements ElectionService, BrokerContextAware {
    private static Logger logger = LoggerFactory.getLogger(ElectionManager.class);

    private Map<TopicPartitionGroup, LeaderElection> leaderElections;
    private TransportClient transportClient;

    protected ElectionConfig electionConfig;
    private ClusterManager clusterManager;

    // 发送给一个broker的命令使用同一个连接
    private final Map<String, Transport> sessions = new ConcurrentHashMap<>();
    private ScheduledExecutorService electionTimerExecutor;
    private ExecutorService electionExecutor;

    private EventBus<ElectionEvent> electionEventManager;
    private ElectionMetadataManager electionMetadataManager;
    private ReplicationManager replicationManager;

    private StoreService storeService;
    private Consume consume;
    private BrokerMonitor brokerMonitor;
    private BrokerContext brokerContext;
    private BrokerConfig brokerConfig;


    public ElectionManager() {
    }

    public ElectionManager(BrokerConfig brokerConfig, ElectionConfig electionConfig, StoreService storeService,
                           Consume consume, ClusterManager clusterManager, BrokerMonitor brokerMonitor) {
        this.brokerConfig = brokerConfig;
        this.electionConfig = electionConfig;
        this.clusterManager = clusterManager;
        this.storeService = storeService;
        this.consume = consume;
        this.brokerMonitor = brokerMonitor;

    }

    public ElectionManager(ElectionConfig electionConfig, StoreService storeService, Consume consume,
                           ClusterManager clusterManager, BrokerMonitor brokerMonitor) {
        this(null,electionConfig, storeService, consume, clusterManager, brokerMonitor);

    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (brokerConfig == null){
             brokerConfig = brokerContext ==null?null:brokerContext.getBrokerConfig();
        }
        if (electionConfig == null) {
            electionConfig = new ElectionConfig(brokerContext == null ? null : brokerContext.getPropertySupplier());
        }
        if (storeService == null && brokerContext != null) {
            storeService = brokerContext.getStoreService();
        }
        if (clusterManager == null && brokerContext != null) {
            clusterManager = brokerContext.getClusterManager();
        }

        if (consume == null && brokerContext != null) {
            consume = brokerContext.getConsume();
        }

        if (brokerMonitor == null && brokerContext != null) {
            brokerMonitor = brokerContext.getBrokerMonitor();
        }

        if (brokerMonitor == null) {
            logger.warn("broker monitor is null.");
        }

        Preconditions.checkArgument(electionConfig != null, "election config is null");
        Preconditions.checkArgument(clusterManager != null, "cluster manager is null");
        Preconditions.checkArgument(storeService != null, "store service is null");
        Preconditions.checkArgument(consume != null, "consume is null");
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();

        electionEventManager = new EventBus<>("LeaderElectionEvent");
        electionEventManager.start();

        leaderElections = new ConcurrentHashMap<>();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setIoThreadName("journalqelection-io-eventLoop");
        transportClient = new BrokerTransportClientFactory().create(clientConfig);
        transportClient.start();

        EventListener<TransportEvent> clientEventListener = new ClientEventListener();
        transportClient.addListener(clientEventListener);

        electionTimerExecutor = Executors.newScheduledThreadPool(electionConfig.getTimerScheduleThreadNum());
        electionExecutor = new ThreadPoolExecutor(electionConfig.getExecutorThreadNumMin(), electionConfig.getExecutorThreadNumMax(),
                60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(electionConfig.getCommandQueueSize()),
                new NamedThreadFactory("Election-sendCommand"));

        replicationManager = new ReplicationManager(electionConfig, storeService, consume, brokerMonitor);
        replicationManager.start();

		Thread.sleep(1000);

        electionMetadataManager = new ElectionMetadataManager(electionConfig.getMetadataFile(), electionConfig.getMetadataPath());
        electionMetadataManager.recover(this);

        logger.info("Election manager started.");
    }

    @Override
    public void doStop() {
        logger.info("Election manager stop");

        for (TopicPartitionGroup topicPartitionGroup : leaderElections.keySet()) {
            LeaderElection leaderElection = getLeaderElection(topicPartitionGroup.getTopic(),
                    topicPartitionGroup.getPartitionGroupId());
            if (leaderElection != null) {
                leaderElection.stop();
            }
        }
        leaderElections.clear();
        sessions.clear();

        Close.close(electionTimerExecutor);
        Close.close(electionExecutor);
        Close.close(electionEventManager);
        Close.close(transportClient);
        Close.close(replicationManager);

        super.doStop();
    }

    @Override
    public void onPartitionGroupCreate(PartitionGroup.ElectType electType, TopicName topic,
                                       int partitionGroup, List<Broker> brokers, Set<Integer> learners, int localBroker, int leader)
            throws ElectionException {

        logger.info("Create election of topic {}, partition group {}, election type is {}" +
                        ", localBroker is {}, leader is {}, all nodes is {}",
                topic, partitionGroup, electType, localBroker, leader, JSON.toJSONString(brokers));

        List<DefaultElectionNode> allNodes = brokers.stream().map(b ->
                new DefaultElectionNode(b.getIp() + ":" + b.getBackEndPort(), b.getId()))
                .collect(Collectors.toList());
        ReplicaGroup replicaGroup = replicationManager.createReplicaGroup(topic.getFullName(), partitionGroup,
                allNodes, learners, localBroker, leader, brokerMonitor);
        LeaderElection leaderElection = createLeaderElection(electType, topic.getFullName(), partitionGroup,
                allNodes, learners, localBroker, leader, replicaGroup);
        replicaGroup.setLeaderElection(leaderElection);

    }

    @Override
    public void onPartitionGroupRemove(TopicName topic, int partitionGroup) {
        logger.info("Remove election of topic {}, partition group {}",
                topic, partitionGroup);
        removeLeaderElection(topic.getFullName(), partitionGroup);
        replicationManager.removeReplicaGroup(topic.getFullName(), partitionGroup);
    }

    @Override
    public void onNodeAdd(TopicName topic, int partitionGroup, PartitionGroup.ElectType electType, List<Broker> brokers,
                          Set<Integer> learners, Broker broker, int localBroker, int leader) throws ElectionException {

        logger.info("Add node {} to election of topic {}, partition group {}", broker, topic, partitionGroup);

        LeaderElection leaderElection = getLeaderElection(topic, partitionGroup);
        if (leaderElection == null) {
            logger.warn("Add node to election of topic {}/partition group {}, leader election is null",
                    topic, partitionGroup);
            if (localBroker != broker.getId()) {
                throw new ElectionException(String.format("Add node to election of topic %s/partition " +
                        "group %d, leader election is null", topic, partitionGroup));
            } else {
                List<DefaultElectionNode> allNodes = brokers.stream().map(b ->
                        new DefaultElectionNode(b.getIp() + ":" + b.getBackEndPort(), b.getId()))
                        .collect(Collectors.toList());
                ReplicaGroup replicaGroup = replicationManager.createReplicaGroup(topic.getFullName(),
                        partitionGroup, allNodes, learners, localBroker, leader, brokerMonitor);
                LeaderElection leaderElectionNew = createLeaderElection(electType, topic.getFullName(), partitionGroup, allNodes, learners,
                        localBroker, leader, replicaGroup);
                replicaGroup.setLeaderElection(leaderElectionNew);
                return;
            }
        }
        leaderElection.addNode(new DefaultElectionNode(broker.getIp() + ":" + broker.getBackEndPort(), broker.getId()));
    }

    @Override
    public void onNodeRemove(TopicName topic, int partitionGroup, int brokerId, int localBroker) {
        logger.info("Remove node {} from election of topic {}, partition group {}",
                brokerId, topic, partitionGroup);
        if (brokerId == localBroker) {
            removeLeaderElection(topic.getFullName(), partitionGroup);
            return;
        }

        LeaderElection leaderElection = getLeaderElection(topic, partitionGroup);
        if (leaderElection == null) {
            logger.warn("Remove node from election of topic {}/partition group {}, " +
                            "leader election is null",
                    topic, partitionGroup);
            return;
        }

        leaderElection.removeNode(brokerId);
    }

    @Override
    public void onElectionTypeChange(TopicName topic, int partitionGroup, PartitionGroup.ElectType electType,
                                     List<Broker> brokers, Set<Integer> learners, int localBroker, int leader)
            throws ElectionException {
        logger.info("Election of topic {} partition group {}'s election type change to {}",
                topic, partitionGroup, electType);
        LeaderElection leaderElection = getLeaderElection(topic, partitionGroup);
        ReplicaGroup replicaGroup = leaderElection.getReplicaGroup();
        removeLeaderElection(topic.getFullName(), partitionGroup);
        try {
            List<DefaultElectionNode> allNodes = brokers.stream()
                    .map(b -> new DefaultElectionNode(b.getIp() + ":" + b.getBackEndPort(), b.getId()))
                    .collect(Collectors.toList());
            LeaderElection leaderElectionNew = createLeaderElection(electType, topic.getFullName(), partitionGroup, allNodes, learners,
                    localBroker, leader, replicaGroup);
            replicaGroup.setLeaderElection(leaderElectionNew);
        } catch (Exception e) {
            throw new ElectionException("Create leader election failed", e);
        }
    }

    @Override
    public void onLeaderChange(TopicName topic, int partitionGroup, int leaderId) throws Exception {
        logger.info("Election of topic {} partition group {}'s leader change to {}",
                topic, partitionGroup, leaderId);
        LeaderElection leaderElection = getLeaderElection(topic, partitionGroup);
        if (leaderElection == null) {
            logger.warn("Leader of topic {}/partition group {} change, election is null",
                    topic, partitionGroup);
            throw new ElectionException(String.format("Leader of topic %s/partition group %d change, " +
                    "election is null", topic, partitionGroup));
        }
        leaderElection.setLeaderId(leaderId);
    }

    /**
     * 根据PartitionGroup查询election
     *
     * @param topic          topic
     * @param partitionGroup partition group
     * @return 查询到的election
     */
    @Override
    public LeaderElection getLeaderElection(TopicName topic, int partitionGroup) {
        return getLeaderElection(topic.getFullName(), partitionGroup);
    }

    public LeaderElection getLeaderElection(String topic, int partitionGroup) {
        return leaderElections.get(new TopicPartitionGroup(topic, partitionGroup));
    }

    /**
     * 恢复选举的元数据
     *
     * @param topicPartitionGroup topic和partition group
     * @param metadata            元数据
     * @throws Exception 异常
     */
    void restoreLeaderElection(TopicPartitionGroup topicPartitionGroup, ElectionMetadata metadata) throws Exception {
        logger.info("Restore election of topic {}, partition group {}, election type is {}" +
                        ", localBroker is {}, leader is {}, all nodes is {}",
                topicPartitionGroup.getTopic(), topicPartitionGroup.getPartitionGroupId(), metadata.getElectType(),
                metadata.getLocalNodeId(), metadata.getLeaderId(), JSON.toJSONString(metadata.getAllNodes()));

        ReplicaGroup replicaGroup = replicationManager.createReplicaGroup(topicPartitionGroup.getTopic(),
                topicPartitionGroup.getPartitionGroupId(),
                new LinkedList<>(metadata.getAllNodes()),
                metadata.getLearners(), metadata.getLocalNodeId(),
                metadata.getLeaderId(), brokerMonitor);
        LeaderElection leaderElection = createLeaderElection(metadata.getElectType(), topicPartitionGroup.getTopic(),
                topicPartitionGroup.getPartitionGroupId(),
                new LinkedList<>(metadata.getAllNodes()),
                metadata.getLearners(), metadata.getLocalNodeId(),
                metadata.getLeaderId(), replicaGroup);
        replicaGroup.setLeaderElection(leaderElection);
    }

    /**
     * 获取LeaderElection数量
     *
     * @return leader election数量
     */
    int getLeaderElectionCount() {
        return leaderElections.size();
    }

    /**
     * 根据schema创建election，fixed或者raft
     *
     * @param topic          topic
     * @param partitionGroup partition group id
     * @return 创建的leader election
     */
    private synchronized LeaderElection createLeaderElection(PartitionGroup.ElectType electType, String topic,
                                                             int partitionGroup, List<DefaultElectionNode> allNodes, Set<Integer> learners,
                                                             int localNodeId, int leaderId, ReplicaGroup replicaGroup) throws ElectionException {

        TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup(topic, partitionGroup);
        LeaderElection leaderElection = leaderElections.get(topicPartitionGroup);
        if (leaderElection != null) {
            logger.warn("Create leader election for topic {}/partition group {}, election is not null",
                    topic, partitionGroup);
            removeLeaderElection(topic, partitionGroup);
        }

        ReplicableStore replicableStore = storeService.getReplicableStore(topic, partitionGroup);
        if (replicableStore == null) {
            throw new ElectionException(String.format("Replicable store of topic %s partition group " +
                    "%d is null", topic, partitionGroup));
        }

        if (electType == PartitionGroup.ElectType.fix) {
            leaderElection = new FixLeaderElection(topicPartitionGroup, electionConfig, this, clusterManager,
                    electionMetadataManager, replicableStore, replicaGroup, electionEventManager, leaderId,
                    localNodeId, allNodes);
        } else if (electType == PartitionGroup.ElectType.raft) {
            leaderElection = new RaftLeaderElection(topicPartitionGroup, electionConfig, this, clusterManager,
                    electionMetadataManager, replicableStore, replicaGroup, electionTimerExecutor,
                    electionExecutor, electionEventManager, localNodeId, allNodes, learners);
        } else {
            throw new ElectionException("Incorrect election type {}" + electType);
        }

        try {
            leaderElection.start();
        } catch (Exception e) {
            throw new ElectionException("Leader election start fail" + e);
        }
        leaderElections.put(topicPartitionGroup, leaderElection);

        return leaderElection;

    }

    /**
     * 根据PartitionGroup删除election
     *
     * @param topic            topic
     * @param partitionGroupId partition group id
     */
    void removeLeaderElection(String topic, int partitionGroupId) {
        synchronized (this) {
            TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup(topic, partitionGroupId);
            LeaderElection leaderElection = leaderElections.get(topicPartitionGroup);
            if (leaderElection == null) {
                logger.warn("Remove leader election of partition group {}, leader election is null",
                        topicPartitionGroup);
                return;
            }

            electionMetadataManager.removeElectionMetadata(topicPartitionGroup);

            leaderElection.stop();
            leaderElections.remove(topicPartitionGroup);

        }
    }

    /**
     * 添加监听者
     *
     * @param listener 监听者
     */
    @Override
    public void addListener(EventListener<ElectionEvent> listener) {
        electionEventManager.addListener(listener);
    }

    /**
     * 删除监听者
     *
     * @param listener 监听者
     */
    @Override
    public void removeListener(EventListener<ElectionEvent> listener) {
        electionEventManager.removeListener(listener);
    }


    @Override
    public String describe(String topic, int partitionGroup) {
        return electionMetadataManager.describe(topic, partitionGroup);
    }

    @Override
    public String describe() {
        return electionMetadataManager.describe();
    }

    /**
     * 从name service恢复选举元数据
     *
     * 只在出问题后恢复使用，执行命令后需要重启broker
     */
    @Override
    public void syncElectionMetadataFromNameService() {
        electionMetadataManager.syncElectionMetadataFromNameService(clusterManager);
    }

    /**
     * 更新term
     * @param topic topic
     * @param partitionGroup partition group
     * @param term 新的term
     */
    @Override
    public void updateTerm(String topic, int partitionGroup, int term) {
        electionMetadataManager.updateTerm(topic, partitionGroup, term);
    }

    /**
     * 向目标节点发送命令，采用异步方式
     *
     * @param address 目标broker地址, ip + ":" + port
     * @param command 发送的命令
     * @throws TransportException 异常
     */
    void sendCommand(String address, Command command, int timeout, CommandCallback callback) throws TransportException {
        if (!isStarted()) {
            logger.info("Send election command but election manager is stopped");
            return;
        }

        Transport transport = sessions.get(address);
        if (transport == null) {
            synchronized (sessions) {
                transport = sessions.get(address);
                if (transport == null) {
                    logger.info("Send election command, create transport of address {}", address);

                    transport = transportClient.createTransport(address);
                    TransportAttribute attribute = transport.attr();
                    if (attribute == null) {
                        attribute = new DefaultTransportAttribute();
                        transport.attr(attribute);
                    }
                    attribute.set("address", address);
                    sessions.put(address, transport);
                }
            }
        }

        transport.async(command, timeout, callback);
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    private class ClientEventListener implements EventListener<TransportEvent> {
        @Override
        public void onEvent(TransportEvent event) {
            switch (event.getType()) {
                case CONNECT:
                    break;
                case EXCEPTION:
                case CLOSE:
                    TransportAttribute attribute = event.getTransport().attr();
                    sessions.remove(attribute.get("address"));
                    logger.info("Election manager transport of {} closed", (String) attribute.get("address"));
                    break;
                default:
                    break;
            }
        }
    }
}
