package com.jd.journalq.broker.jmq;

import com.jd.journalq.broker.jmq.coordinator.assignment.PartitionAssignmentHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.jmq.config.JMQConfig;
import com.jd.journalq.broker.jmq.coordinator.Coordinator;
import com.jd.journalq.broker.jmq.coordinator.GroupMetadataManager;
import com.jd.journalq.broker.polling.LongPollingManager;

/**
 * JMQContext
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class JMQContext {

    private static JMQConfig config;
    private Coordinator coordinator;
    private GroupMetadataManager coordinatorGroupManager;
    private PartitionAssignmentHandler partitionAssignmentHandler;
    private LongPollingManager longPollingManager;
    private BrokerContext brokerContext;

    public JMQContext(JMQConfig config, Coordinator coordinator, GroupMetadataManager coordinatorGroupManager, PartitionAssignmentHandler partitionAssignmentHandler,
                      LongPollingManager longPollingManager, BrokerContext brokerContext) {
        this.config = config;
        this.coordinator = coordinator;
        this.coordinatorGroupManager = coordinatorGroupManager;
        this.partitionAssignmentHandler = partitionAssignmentHandler;
        this.longPollingManager = longPollingManager;
        this.brokerContext = brokerContext;
    }

    public static JMQConfig getConfig() {
        return config;
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public GroupMetadataManager getCoordinatorGroupManager() {
        return coordinatorGroupManager;
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