package org.joyqueue.broker.consumer.position;

import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.command.ReplicateConsumePosRequest;
import org.joyqueue.broker.consumer.command.ReplicateConsumePosResponse;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.position.model.Position;
import org.joyqueue.broker.network.session.BrokerTransportManager;
import org.joyqueue.broker.network.session.BrokerTransportSession;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author LiYue
 * Date: 2019/12/12
 */
class ConsumePositionReplicator {
    private static final Logger logger = LoggerFactory.getLogger(ConsumePositionReplicator.class);
    private final StoreService storeService;
    private final Consume consume;
    private final BrokerTransportManager brokerTransportManager;
    private final ClusterManager clusterManager;

    ConsumePositionReplicator(StoreService storeService, Consume consume, BrokerTransportManager brokerTransportManager, ClusterManager clusterManager) {
        this.storeService = storeService;
        this.consume = consume;
        this.brokerTransportManager = brokerTransportManager;
        this.clusterManager = clusterManager;
    }

    void replicateConsumePosition() {
        storeService.getAllStores().stream()
                .filter(PartitionGroupStore::writable)
                .forEach(store -> {
                    try {
                        TopicName topic = TopicName.parse(store.getTopic());
                        int group = store.getPartitionGroup();
                        int localReplicaId = clusterManager.getBrokerId();
                        Map<ConsumePartition, Position> consumePositions = consume.getConsumePositionByGroup(TopicName.parse(store.getTopic()), store.getPartitionGroup());
                        if (consumePositions == null) {
                            logger.warn("Partition group {}/node {} get consumer info return null",
                                    store.getTopic(), clusterManager.getBrokerId());
                            return;
                        }

                        ReplicateConsumePosRequest request = new ReplicateConsumePosRequest(consumePositions);
                        JoyQueueHeader header = new JoyQueueHeader(Direction.REQUEST, CommandType.REPLICATE_CONSUME_POS_REQUEST);
                        Command command = new Command(header, request);

                        PartitionGroup partitionGroup = clusterManager.getPartitionGroup(topic, group);
                        if(partitionGroup == null) {
                            logger.warn("Partition group not found in cluster manager! Topic: {}, group: {}.", topic.getFullName(), group);
                            return;
                        }

                        partitionGroup.getReplicas().stream()
                                .filter(r -> r != localReplicaId)
                                .map(clusterManager::getBrokerById)
                                .forEach(broker -> {
                                    try {
                                        BrokerTransportSession session = brokerTransportManager.getOrCreateSession(broker);
                                        session.async(command, new ReplicateConsumePosRequestCallback(topic.getFullName(), group, localReplicaId, broker.getId()));
                                    } catch (Throwable t) {
                                        logger.warn("Partition group {}-{}/node {} send replicate consume pos message fail",
                                                topic.getFullName(), group, localReplicaId, t);

                                    }
                                });
                    } catch (Throwable e) {
                        logger.warn("Partition group {}-{} send replicate consume pos message fail",
                                store.getTopic(), store.getPartitionGroup(), e);
                    }

                });
    }

    /**
     * Callback of replicate consume pos request command
     */
    private static class ReplicateConsumePosRequestCallback implements CommandCallback {
        private final int remoteBrokerId;
        private final String topic;
        private final int group;
        private final int localBrokerId;
        ReplicateConsumePosRequestCallback(String topic, int group, int localBrokerId, int remoteBrokerId) {
            this.topic = topic;
            this.group = group;
            this.localBrokerId = localBrokerId;
            this.remoteBrokerId = remoteBrokerId;
        }

        @Override
        public void onSuccess(Command request, Command responseCommand) {
            if (!(responseCommand.getPayload() instanceof ReplicateConsumePosResponse)) {
                return;
            }
            ReplicateConsumePosResponse response = (ReplicateConsumePosResponse)responseCommand.getPayload();
            if (!response.isSuccess()) {
                logger.info("Partition group {}-{}/node {} replicate consume pos to {} fail",
                        topic, group, localBrokerId, remoteBrokerId);
            }
        }

        @Override
        public void onException(Command request, Throwable cause) {
            logger.info("Partition group {}-{}/node {} replicate consume pos to {} fail",
                    topic, group, localBrokerId, remoteBrokerId, cause);
        }
    }

}
