package com.jd.journalq.broker.manage;

import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.coordinator.CoordinatorService;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.manage.exporter.BrokerManageExporter;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.broker.archive.ArchiveManager;
import com.jd.journalq.broker.manage.config.BrokerManageConfig;
import com.jd.journalq.broker.monitor.BrokerMonitorService;
import com.jd.journalq.server.retry.api.MessageRetry;
import com.jd.journalq.store.StoreManagementService;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.service.Service;

/**
 * brokermonitor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public class BrokerManageService extends Service {

    private BrokerManageConfig config;
    private BrokerManageServiceManager brokerManageServiceManager;
    private BrokerManageExporter brokerManageExporter;
    private StoreService storeService;
    private MessageRetry retryManager;
    private CoordinatorService coordinatorService;
    private ArchiveManager archiveManager;

    public BrokerManageService(BrokerManageConfig config, BrokerMonitorService brokerMonitorService, ClusterManager clusterManager, StoreManagementService storeManagementService, StoreService storeService, Consume consume, MessageRetry retryManager, CoordinatorService coordinatorService, ArchiveManager archiveManager, NameService nameService, ElectionService electionManager) {
        this.config = config;
        this.storeService = storeService;
        this.retryManager = retryManager;
        this.coordinatorService = coordinatorService;
        this.brokerManageServiceManager = new BrokerManageServiceManager(brokerMonitorService.getBrokerMonitor(),clusterManager, storeManagementService, storeService, consume, retryManager, coordinatorService, archiveManager, nameService, electionManager);
        this.brokerManageExporter = new BrokerManageExporter(config, brokerManageServiceManager);
    }

    public void registerService(String key, Object service) {
        brokerManageExporter.registerService(key, service);
    }

    @Override
    protected void doStart() throws Exception {
        brokerManageServiceManager.start();
        brokerManageExporter.start();
    }

    @Override
    protected void doStop() {
        if (brokerManageExporter != null) {
            brokerManageExporter.stop();
        }
        if (brokerManageServiceManager != null) {
            brokerManageServiceManager.stop();
        }
    }

    public BrokerManageConfig getConfig() {
        return config;
    }

    public BrokerManageServiceManager getBrokerManageServiceManager() {
        return brokerManageServiceManager;
    }
}