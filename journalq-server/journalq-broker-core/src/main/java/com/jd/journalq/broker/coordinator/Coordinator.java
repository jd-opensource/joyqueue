package com.jd.journalq.broker.coordinator;

import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.broker.coordinator.domain.CoordinatorDetail;
import com.jd.journalq.broker.coordinator.session.CoordinatorSessionManager;
import com.jd.journalq.broker.coordinator.support.CoordinatorInitializer;
import com.jd.journalq.broker.coordinator.support.CoordinatorResolver;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.TopicName;

/**
 * Coordinator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class Coordinator {

    private CoordinatorConfig config;
    private ClusterManager clusterManager;
    private CoordinatorResolver coordinatorResolver;
    private CoordinatorInitializer coordinatorInitializer;
    private CoordinatorSessionManager coordinatorSessionManager;

    public Coordinator(CoordinatorConfig config, ClusterManager clusterManager, CoordinatorResolver coordinatorResolver, CoordinatorInitializer coordinatorInitializer, CoordinatorSessionManager coordinatorSessionManager) {
        this.config = config;
        this.clusterManager = clusterManager;
        this.coordinatorResolver = coordinatorResolver;
        this.coordinatorInitializer = coordinatorInitializer;
        this.coordinatorSessionManager = coordinatorSessionManager;
    }

    // group

    public boolean isCurrentGroupCoordinator(String group) {
        Broker coordinatorBroker = findGroupCoordinator(group);
        return clusterManager.getBroker().equals(coordinatorBroker);
    }

    public Broker findGroupCoordinator(String group) {
        return coordinatorResolver.findCoordinator(group, config.getGroupTopic());
    }

    public CoordinatorDetail getGroupCoordinatorDetail(String group) {
        return coordinatorResolver.getCoordinatorDetail(group, config.getGroupTopic());
    }

    public boolean isGroupCoordinatorTopic(TopicName group) {
        return config.getGroupTopic().getFullName().equals(group);
    }

    // transaction

    public boolean isCurrentTransactionCoordinator(String key) {
        Broker coordinatorBroker = findTransactionCoordinator(key);
        return clusterManager.getBroker().equals(coordinatorBroker);
    }

    public Broker findTransactionCoordinator(String key) {
        return coordinatorResolver.findCoordinator(key, config.getGroupTopic());
    }

    public CoordinatorDetail getTransactionCoordinatorDetail(String key) {
        return coordinatorResolver.getCoordinatorDetail(key, config.getGroupTopic());
    }

    public boolean isTransactionCoordinatorTopic(TopicName topic) {
        return config.getTransactionTopic().getFullName().equals(topic);
    }

    public boolean initCoordinator() {
        return coordinatorInitializer.init();
    }

    public CoordinatorSessionManager getSessionManager() {
        return coordinatorSessionManager;
    }
}