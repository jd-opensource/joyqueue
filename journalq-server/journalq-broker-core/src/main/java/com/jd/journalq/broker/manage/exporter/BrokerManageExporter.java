package com.jd.journalq.broker.manage.exporter;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.manage.BrokerManageServiceManager;
import com.jd.journalq.broker.manage.config.BrokerManageConfig;
import com.jd.journalq.toolkit.service.Service;

import java.util.Map;

/**
 * BrokerManageExporter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public class BrokerManageExporter extends Service {

    private BrokerManageConfig config;
    private BrokerManageExportServer brokerManageExportServer;
    private BrokerManageServiceManager brokerManageServiceManager;
    private Map<String, Object> serviceMap = Maps.newHashMap();

    public BrokerManageExporter(BrokerManageConfig config, BrokerManageServiceManager brokerManageServiceManager) {
        this.config = config;
        this.brokerManageServiceManager = brokerManageServiceManager;
    }

    public void registerService(String key, Object service) {
        serviceMap.put(key, service);
    }

    @Override
    protected void validate() throws Exception {
        brokerManageExportServer = new BrokerManageExportServer(config);
        brokerManageExportServer.registerServices(serviceMap);
        brokerManageExportServer.registerService("brokerMonitorService", brokerManageServiceManager.getBrokerMonitorService());
        brokerManageExportServer.registerService("brokerManageService", brokerManageServiceManager.getBrokerManageService());
    }

    @Override
    protected void doStart() throws Exception {
        brokerManageExportServer.start();
    }

    @Override
    protected void doStop() {
        brokerManageExportServer.stop();
    }
}