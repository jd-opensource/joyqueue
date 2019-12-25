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
package org.joyqueue.broker.store;

import com.google.common.base.Preconditions;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.config.BrokerStoreConfig;
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.nsr.NameService;
import org.joyqueue.store.StoreService;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 存储管理器
 */
public class StoreManager extends Service implements BrokerContextAware, PropertySupplierAware {
    private static final Logger logger = LoggerFactory.getLogger(StoreManager.class);

    private BrokerContext brokerContext;
    private PropertySupplier propertySupplier;
    private NameService nameService;
    private ClusterManager clusterManager;
    private StoreService storeService;
    private StoreCleanManager storeCleanManager;
    private ElectionService electionService;

    private BrokerStoreConfig config;

    public StoreManager(StoreService storeService , NameService nameService, ClusterManager clusterManager, ElectionService electionService) {
        this.storeService = storeService;
        this.nameService = nameService;
        this.clusterManager = clusterManager;
        this.electionService = electionService;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        Preconditions.checkArgument(brokerContext != null, "broker context can not be null");
        config = new BrokerStoreConfig(brokerContext.getPropertySupplier());
        storeCleanManager = new StoreCleanManager(
                propertySupplier,
                storeService,
                clusterManager,
                this.brokerContext.getPositionManager()
        );
    }

    @Override
    public void doStop() {
        storeCleanManager.stop();
    }

    @Override
    public void doStart() throws Exception {
        storeCleanManager.start();
    }

    public NameService getNameService() {
        return nameService;
    }

    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
    }
}
