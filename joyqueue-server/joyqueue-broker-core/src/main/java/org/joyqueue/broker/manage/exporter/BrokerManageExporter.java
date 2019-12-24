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
package org.joyqueue.broker.manage.exporter;

import com.google.common.collect.Maps;
import org.joyqueue.broker.manage.BrokerManageServiceManager;
import org.joyqueue.broker.manage.config.BrokerManageConfig;
import org.joyqueue.toolkit.service.Service;

import java.util.Map;

/**
 * BrokerManageExporter
 *
 * author: gaohaoxiang
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