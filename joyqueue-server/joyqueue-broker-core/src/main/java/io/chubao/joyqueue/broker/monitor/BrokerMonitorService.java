package io.chubao.joyqueue.broker.monitor;

import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.monitor.config.BrokerMonitorConfig;
import io.chubao.joyqueue.toolkit.service.Service;

/**
 * brokermonitor
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class BrokerMonitorService extends Service {

    private BrokerMonitorConfig config;
    private SessionManager sessionManager;
    private BrokerMonitor brokerMonitor;
    private BrokerStatManager brokerStatManager;
    private BrokerStatSaveScheduler brokerStatSaveScheduler;
    private BrokerMonitorSlicer brokerMonitorSlicer;

    public BrokerMonitorService(Integer brokerId, BrokerMonitorConfig config, SessionManager sessionManager, ClusterManager clusterManager) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.brokerStatManager = new BrokerStatManager(brokerId, config);
        this.brokerMonitor = new BrokerMonitor(config, sessionManager, brokerStatManager, clusterManager);
        this.brokerStatSaveScheduler = new BrokerStatSaveScheduler(config, brokerStatManager);
        this.brokerMonitorSlicer = new BrokerMonitorSlicer(brokerMonitor);
    }

    @Override
    protected void doStart() throws Exception {
        brokerStatManager.start();
        brokerMonitor.start();
        brokerStatSaveScheduler.start();
        brokerMonitorSlicer.start();
    }

    @Override
    protected void doStop() {
        brokerStatManager.stop();
        brokerStatSaveScheduler.stop();
        brokerMonitor.stop();
        brokerMonitorSlicer.stop();
    }

    public BrokerMonitor getBrokerMonitor() {
        return brokerMonitor;
    }

    public BrokerMonitorConfig getConfig() {
        return config;
    }
}