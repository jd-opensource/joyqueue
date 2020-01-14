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
package org.joyqueue.broker.monitor;

import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.monitor.config.BrokerMonitorConfig;
import org.joyqueue.toolkit.service.Service;

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