package com.jd.journalq.broker.coordinator;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.broker.coordinator.group.GroupMetadataManager;
import com.jd.journalq.broker.coordinator.transaction.TransactionMetadataManager;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.toolkit.service.Service;

import java.util.concurrent.ConcurrentMap;

/**
 * CoordinatorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class CoordinatorService extends Service {

    private CoordinatorConfig config;
    private ClusterManager clusterManager;
    private NameService nameService;

    private CoordinatorInitializer coordinatorInitializer;
    private CoordinatorResolver coordinatorResolver;
    private Coordinator coordinator;

    private final ConcurrentMap<String, GroupMetadataManager> groupMetadataManagerMap = Maps.newConcurrentMap();
    private final ConcurrentMap<String, TransactionMetadataManager> transactionMetadataManagerMap = Maps.newConcurrentMap();

    public CoordinatorService(CoordinatorConfig config, ClusterManager clusterManager, NameService nameService) {
        this.config = config;
        this.clusterManager = clusterManager;
        this.nameService = nameService;
        this.coordinatorInitializer = new CoordinatorInitializer(config, clusterManager, nameService);
        this.coordinatorResolver = new CoordinatorResolver(config, clusterManager);
        this.coordinator = new Coordinator(config, clusterManager, coordinatorResolver, coordinatorInitializer);
    }

    @Override
    protected void doStart() throws Exception {
        coordinatorInitializer.init();
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public GroupMetadataManager getOrCreateGroupMetadataManager(String namespace) {
        GroupMetadataManager groupMetadataManager = groupMetadataManagerMap.get(namespace);
        if (groupMetadataManager == null) {
            groupMetadataManager = doCreateGroupMetadataManager(namespace);
            GroupMetadataManager oldGroupMetadataManager = groupMetadataManagerMap.putIfAbsent(namespace, groupMetadataManager);
            if (oldGroupMetadataManager != null) {
                groupMetadataManager = oldGroupMetadataManager;
            }
        }
        return groupMetadataManager;
    }

    public TransactionMetadataManager getOrCreateTransactionMetadataManager(String namespace) {
        TransactionMetadataManager transactionMetadataManager = transactionMetadataManagerMap.get(namespace);
        if (transactionMetadataManager == null) {
            transactionMetadataManager = doCreateTransactionMetadataManager(namespace);
            TransactionMetadataManager oldTransactionMetadataManager = transactionMetadataManagerMap.putIfAbsent(namespace, transactionMetadataManager);
            if (oldTransactionMetadataManager != null) {
                transactionMetadataManager = oldTransactionMetadataManager;
            }
        }
        return transactionMetadataManager;
    }

    protected TransactionMetadataManager doCreateTransactionMetadataManager(String namespace) {
        return new TransactionMetadataManager(namespace, config);
    }

    protected GroupMetadataManager doCreateGroupMetadataManager(String namespace) {
        return new GroupMetadataManager(namespace, config);
    }
}