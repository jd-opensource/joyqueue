package io.chubao.joyqueue.broker.protocol;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.polling.LongPollingManager;
import io.chubao.joyqueue.broker.protocol.config.JoyQueueConfig;
import io.chubao.joyqueue.broker.protocol.coordinator.Coordinator;
import io.chubao.joyqueue.broker.protocol.coordinator.GroupMetadataManager;
import io.chubao.joyqueue.broker.protocol.coordinator.assignment.PartitionAssignmentHandler;

/**
 * JoyQueueContext
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class JoyQueueContext {

    private static JoyQueueConfig config;
    private Coordinator coordinator;
    private GroupMetadataManager groupMetadataManager;
    private PartitionAssignmentHandler partitionAssignmentHandler;
    private LongPollingManager longPollingManager;
    private BrokerContext brokerContext;

    public JoyQueueContext(JoyQueueConfig config, Coordinator coordinator, GroupMetadataManager groupMetadataManager, PartitionAssignmentHandler partitionAssignmentHandler,
                           LongPollingManager longPollingManager, BrokerContext brokerContext) {
        this.config = config;
        this.coordinator = coordinator;
        this.groupMetadataManager = groupMetadataManager;
        this.partitionAssignmentHandler = partitionAssignmentHandler;
        this.longPollingManager = longPollingManager;
        this.brokerContext = brokerContext;
    }

    public static JoyQueueConfig getConfig() {
        return config;
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public GroupMetadataManager getGroupMetadataManager() {
        return groupMetadataManager;
    }

    public PartitionAssignmentHandler getPartitionAssignmentHandler() {
        return partitionAssignmentHandler;
    }

    public LongPollingManager getLongPollingManager() {
        return longPollingManager;
    }

    public BrokerContext getBrokerContext() {
        return brokerContext;
    }
}