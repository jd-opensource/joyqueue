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
package org.joyqueue.broker.coordinator;

import com.google.common.collect.Maps;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.coordinator.config.CoordinatorConfig;
import org.joyqueue.broker.coordinator.config.CoordinatorConfigKey;
import org.joyqueue.broker.coordinator.group.GroupMetadataManager;
import org.joyqueue.broker.coordinator.support.CoordinatorInitializer;
import org.joyqueue.broker.coordinator.support.CoordinatorResolver;
import org.joyqueue.broker.coordinator.transaction.TransactionMetadataManager;
import org.joyqueue.broker.network.support.BrokerTransportClientFactory;
import org.joyqueue.network.transport.config.TransportConfigSupport;
import org.joyqueue.network.transport.session.session.TransportSessionManager;
import org.joyqueue.network.transport.session.session.config.TransportSessionConfig;
import org.joyqueue.nsr.NameService;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.service.Service;

import java.util.concurrent.ConcurrentMap;

/**
 * CoordinatorService
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class CoordinatorService extends Service {

    private CoordinatorConfig config;
    private ClusterManager clusterManager;
    private NameService nameService;

    private CoordinatorInitializer coordinatorInitializer;
    private CoordinatorResolver coordinatorResolver;
    private TransportSessionManager coordinatorSessionManager;
    private Coordinator coordinator;

    private final ConcurrentMap<String, GroupMetadataManager> groupMetadataManagerMap = Maps.newConcurrentMap();
    private final ConcurrentMap<String, TransactionMetadataManager> transactionMetadataManagerMap = Maps.newConcurrentMap();

    public CoordinatorService(PropertySupplier propertySupplier, ClusterManager clusterManager, NameService nameService) {
        this.config = new CoordinatorConfig(propertySupplier);
        this.clusterManager = clusterManager;
        this.nameService = nameService;
        this.coordinatorInitializer = new CoordinatorInitializer(config, clusterManager, nameService);
        this.coordinatorResolver = new CoordinatorResolver(config, clusterManager);
        this.coordinatorSessionManager = new TransportSessionManager(new TransportSessionConfig(propertySupplier),
                TransportConfigSupport.buildClientConfig(propertySupplier, CoordinatorConfigKey.TRANSPORT_KEY_PREFIX), new BrokerTransportClientFactory());
        this.coordinator = new Coordinator(config, clusterManager, coordinatorResolver, coordinatorInitializer, coordinatorSessionManager);
    }

    @Override
    protected void doStart() throws Exception {
        coordinatorInitializer.init();
        coordinatorSessionManager.start();
    }

    @Override
    protected void doStop() {
        if (coordinatorSessionManager != null) {
            coordinatorSessionManager.stop();
        }
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