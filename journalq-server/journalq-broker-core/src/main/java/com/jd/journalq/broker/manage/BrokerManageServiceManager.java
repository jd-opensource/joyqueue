package com.jd.journalq.broker.manage;

import com.jd.journalq.broker.archive.ArchiveManager;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.coordinator.CoordinatorService;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.manage.service.BrokerManageService;
import com.jd.journalq.broker.manage.service.ConnectionManageService;
import com.jd.journalq.broker.manage.service.support.DefaultBrokerManageService;
import com.jd.journalq.broker.manage.service.support.DefaultConnectionManageService;
import com.jd.journalq.broker.manage.service.support.DefaultConsumerManageService;
import com.jd.journalq.broker.manage.service.support.DefaultCoordinatorManageService;
import com.jd.journalq.broker.manage.service.support.DefaultElectionManageService;
import com.jd.journalq.broker.manage.service.support.DefaultMessageManageService;
import com.jd.journalq.broker.manage.service.support.DefaultStoreManageService;
import com.jd.journalq.broker.monitor.BrokerMonitor;
import com.jd.journalq.broker.monitor.service.BrokerMonitorService;
import com.jd.journalq.broker.monitor.service.support.DefaultArchiveMonitorService;
import com.jd.journalq.broker.monitor.service.support.DefaultBrokerMonitorInternalService;
import com.jd.journalq.broker.monitor.service.support.DefaultBrokerMonitorService;
import com.jd.journalq.broker.monitor.service.support.DefaultConnectionMonitorService;
import com.jd.journalq.broker.monitor.service.support.DefaultConsumerMonitorService;
import com.jd.journalq.broker.monitor.service.support.DefaultCoordinatorMonitorService;
import com.jd.journalq.broker.monitor.service.support.DefaultMetadataMonitorService;
import com.jd.journalq.broker.monitor.service.support.DefaultPartitionMonitorService;
import com.jd.journalq.broker.monitor.service.support.DefaultProducerMonitorService;
import com.jd.journalq.broker.monitor.service.support.DefaultTopicMonitorService;
import com.jd.journalq.broker.monitor.stat.BrokerStat;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.server.retry.api.MessageRetry;
import com.jd.journalq.store.StoreManagementService;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.service.Service;

/**
 * BrokerManageServiceManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public class BrokerManageServiceManager extends Service {

    private BrokerMonitor brokerMonitor;
    private ClusterManager clusterManager;
    private StoreManagementService storeManagementService;
    private StoreService storeService;
    private Consume consume;
    private BrokerMonitorService brokerMonitorService;
    private BrokerManageService brokerManageService;
    private MessageRetry retryManager;
    private CoordinatorService coordinatorService;
    private ArchiveManager archiveManager;
    private NameService nameService;
    private StoreService store;
    private ElectionService electionManager;

    public BrokerManageServiceManager(BrokerMonitor brokerMonitor, ClusterManager clusterManager, StoreManagementService storeManagementService, StoreService storeService, Consume consume, MessageRetry messageRetry, CoordinatorService coordinatorService, ArchiveManager archiveManager, NameService nameService, StoreService store, ElectionService electionManager) {
        this.brokerMonitor = brokerMonitor;
        this.clusterManager = clusterManager;
        this.storeManagementService = storeManagementService;
        this.storeService = storeService;
        this.consume = consume;
        this.retryManager = messageRetry;
        this.coordinatorService = coordinatorService;
        this.archiveManager = archiveManager;
        this.nameService = nameService;
        this.store = store;
        this.electionManager = electionManager;
    }

    @Override
    protected void validate() throws Exception {
        this.brokerMonitorService = newBrokerMonitorService();
        this.brokerManageService = newBrokerManageService();
    }

    protected BrokerMonitorService newBrokerMonitorService() {
        BrokerStat brokerStat = brokerMonitor.getBrokerStat();
        DefaultBrokerMonitorInternalService brokerMonitorInternalService = new DefaultBrokerMonitorInternalService(brokerStat, consume, storeManagementService, nameService, store, electionManager, clusterManager);
        DefaultConnectionMonitorService connectionMonitorService = new DefaultConnectionMonitorService(brokerStat);
        DefaultConsumerMonitorService consumerMonitorService = new DefaultConsumerMonitorService(brokerStat, consume, storeManagementService, retryManager, clusterManager);
        DefaultProducerMonitorService producerMonitorService = new DefaultProducerMonitorService(brokerStat, storeManagementService, clusterManager);
        DefaultTopicMonitorService topicMonitorService = new DefaultTopicMonitorService(brokerStat,storeManagementService);
        DefaultPartitionMonitorService partitionMonitorService = new DefaultPartitionMonitorService(brokerStat, storeManagementService);
        DefaultCoordinatorMonitorService coordinatorMonitorService = new DefaultCoordinatorMonitorService(coordinatorService);
        DefaultArchiveMonitorService archiveMonitorService = new DefaultArchiveMonitorService(archiveManager);
        DefaultMetadataMonitorService metadataMonitorService = new DefaultMetadataMonitorService(clusterManager);
        return new DefaultBrokerMonitorService(brokerMonitorInternalService, connectionMonitorService, consumerMonitorService, producerMonitorService, topicMonitorService, partitionMonitorService, coordinatorMonitorService, archiveMonitorService, metadataMonitorService);
    }

    protected BrokerManageService newBrokerManageService() {
        ConnectionManageService connectionManageService = new DefaultConnectionManageService(brokerMonitor.getSessionManager());
        DefaultMessageManageService messageManageService = new DefaultMessageManageService(consume, storeManagementService);
        DefaultStoreManageService storeManageService = new DefaultStoreManageService(storeManagementService);
        DefaultConsumerManageService consumerManageService = new DefaultConsumerManageService(consume, storeManagementService, storeService, clusterManager);
        DefaultCoordinatorManageService coordinatorManageService = new DefaultCoordinatorManageService(coordinatorService);
        DefaultElectionManageService electionManageService = new DefaultElectionManageService(electionManager);
        return new DefaultBrokerManageService(connectionManageService, messageManageService, storeManageService, consumerManageService, coordinatorManageService, electionManageService);
    }

    public BrokerManageService getBrokerManageService() {
        return brokerManageService;
    }

    public BrokerMonitorService getBrokerMonitorService() {
        return brokerMonitorService;
    }
}