package com.jd.journalq.broker.monitor;

import com.jd.journalq.broker.monitor.config.BrokerMonitorConfig;
import com.jd.journalq.toolkit.service.Service;

/**
 * brokermonitor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public class BrokerMonitorService extends Service {

    private BrokerMonitorConfig config;
    private SessionManager sessionManager;
    private BrokerMonitor brokerMonitor;
    private BrokerStatManager brokerStatManager;
    private BrokerStatSaveScheduler brokerStatSaveScheduler;

    public BrokerMonitorService(Integer brokerId, BrokerMonitorConfig config, SessionManager sessionManager) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.brokerStatManager = new BrokerStatManager(brokerId, config);
        this.brokerMonitor = new BrokerMonitor(config, sessionManager, brokerStatManager);
        this.brokerStatSaveScheduler = new BrokerStatSaveScheduler(config, brokerStatManager);
    }

    @Override
    protected void doStart() throws Exception {
        brokerStatManager.start();
        brokerMonitor.start();
        brokerStatSaveScheduler.start();
    }

    @Override
    protected void doStop() {
        brokerStatManager.stop();
        brokerStatSaveScheduler.stop();
        brokerMonitor.stop();
    }

    public BrokerMonitor getBrokerMonitor() {
        return brokerMonitor;
    }

    public BrokerMonitorConfig getConfig() {
        return config;
    }
}