/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.manage;

import org.joyqueue.broker.archive.ArchiveManager;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.MessageConvertSupport;
import org.joyqueue.broker.coordinator.CoordinatorService;
import org.joyqueue.broker.manage.config.BrokerManageConfig;
import org.joyqueue.broker.manage.exporter.BrokerManageExporter;
import org.joyqueue.broker.monitor.BrokerMonitorService;
import org.joyqueue.nsr.NameService;
import org.joyqueue.server.retry.api.MessageRetry;
import org.joyqueue.store.StoreManagementService;
import org.joyqueue.store.StoreService;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * brokermonitor
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class BrokerManageService extends Service {
    private static final Logger LOG= LoggerFactory.getLogger(BrokerManageService.class);
    private BrokerManageConfig config;
    private BrokerManageServiceManager brokerManageServiceManager;
    private BrokerManageExporter brokerManageExporter;

    public BrokerManageService(BrokerManageConfig config, BrokerMonitorService brokerMonitorService,
                               ClusterManager clusterManager, StoreManagementService storeManagementService,
                               StoreService storeService, Consume consume,
                               MessageRetry retryManager, CoordinatorService coordinatorService,
                               ArchiveManager archiveManager, NameService nameService,
                               MessageConvertSupport messageConvertSupport) {
        this.config = config;
        this.brokerManageServiceManager = new BrokerManageServiceManager(brokerMonitorService.getBrokerMonitor(),
                clusterManager, storeManagementService,
                storeService, consume,
                retryManager, coordinatorService,
                archiveManager, nameService, messageConvertSupport);
        this.brokerManageExporter = new BrokerManageExporter(config, brokerManageServiceManager);
    }

    public void registerService(String key, Object service) {
        brokerManageExporter.registerService(key, service);
    }

    @Override
    protected void doStart() throws Exception {
        brokerManageServiceManager.start();
        brokerManageExporter.start();
        LOG.info("Broker manage service started");
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