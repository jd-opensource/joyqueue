package com.jd.journalq.broker.replication;

import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.election.DefaultElectionNode;
import com.jd.journalq.broker.election.ElectionConfig;
import com.jd.journalq.broker.election.ElectionException;
import com.jd.journalq.broker.election.TopicPartitionGroup;
import com.jd.journalq.broker.monitor.BrokerMonitor;
import com.jd.journalq.broker.network.support.BrokerTransportClientFactory;
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
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
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
    private final ConcurrentHashMap<String, Transport> sessions = new ConcurrentHashMap<>();

    private StoreService storeService;
    private Consume consume;
    private BrokerMonitor brokerMonitor;

    private TransportClient transportClient;
    private ExecutorService replicateExecutor;

    public ReplicationManager(ElectionConfig electionConfig, StoreService storeService,
                              Consume consume, BrokerMonitor brokerMonitor) {
        Preconditions.checkArgument(electionConfig != null, "election config is null");
        Preconditions.checkArgument(storeService != null, "store service is null");
        Preconditions.checkArgument(consume != null, "consume is null");
        Preconditions.checkArgument(brokerMonitor != null, "broker monitor is null");

        this.electionConfig = electionConfig;
        this.storeService = storeService;
        this.consume = consume;
        this.brokerMonitor = brokerMonitor;
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();

        replicaGroups = new ConcurrentHashMap<>();

        ClientConfig clientConfig = new ClientConfig();
        transportClient = new BrokerTransportClientFactory().create(clientConfig);
        transportClient.start();

        EventListener<TransportEvent> clientEventListener = new ClientEventListener();
        transportClient.addListener(clientEventListener);

        replicateExecutor = new ThreadPoolExecutor(electionConfig.getReplicateThreadNumMin(), electionConfig.getReplicateThreadNumMax(),
                60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(electionConfig.getCommandQueueSize()));
    }

    @Override
    public void doStop() {
        transportClient.stop();
        replicateExecutor.shutdown();

        super.doStop();
    }

    public synchronized ReplicaGroup createReplicaGroup(String topic, int partitionGroup, List<DefaultElectionNode> allNodes,
                          Set<Integer> learners, int localReplicaId, int leaderId, BrokerMonitor brokerMonitor) throws ElectionException {
        TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup(topic, partitionGroup);
        ReplicaGroup replicaGroup = replicaGroups.get(topicPartitionGroup);
        if (replicaGroup != null) {
            logger.warn("Create replica group for topic {} partition group {} failed, " +
                    "replication group is not null", topic, partitionGroup);
            removeReplicaGroup(topic, partitionGroup);
        }

        ReplicableStore replicableStore = storeService.getReplicableStore(topic, partitionGroup);
        if (replicableStore == null) {
            logger.info("Create replica group for topic {} partition group {} failed, " +
                    "replicable store is null", topic, partitionGroup);
            throw new ElectionException(String.format("Create Replica group for topic %s partition group " +
                    "%d failed, replicable store is null", topic, partitionGroup));
        }
        replicaGroup = new ReplicaGroup(topicPartitionGroup, this, replicableStore, electionConfig,
                consume, replicateExecutor, brokerMonitor, allNodes, learners, localReplicaId, leaderId);
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


    /**
     * 向目标节点发送命令，采用异步方式
     * @param address 目标broker地址, ip + ":" + port
     * @param command 要发送的命令
     * @throws TransportException
     */
    void sendCommand(String address, Command command, int timeout, CommandCallback callback) throws TransportException {
        Transport transport = sessions.get(address);
        if (transport == null) {
            synchronized (sessions) {
                transport = sessions.get(address);
                if (transport == null) {
                    logger.info("Replication manager create transport of {}", address);

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
                    logger.info("Replication manager transport of {} closed", (String)attribute.get("address"));
                    break;
                default:
                    break;
            }
        }
    }
}
