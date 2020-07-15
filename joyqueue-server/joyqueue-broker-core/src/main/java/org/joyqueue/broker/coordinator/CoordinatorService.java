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
import org.joyqueue.broker.coordinator.group.GroupMetadataManager;
import org.joyqueue.broker.coordinator.support.CoordinatorInitializer;
import org.joyqueue.broker.coordinator.support.CoordinatorResolver;
import org.joyqueue.broker.coordinator.transaction.TransactionMetadataManager;
import org.joyqueue.broker.network.session.BrokerTransportManager;
import org.joyqueue.nsr.NameService;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * CoordinatorService
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class CoordinatorService extends Service {
    private static final Logger LOG= LoggerFactory.getLogger(CoordinatorService.class);
    private CoordinatorConfig config;
    private ClusterManager clusterManager;
    private NameService nameService;

    private CoordinatorInitializer coordinatorInitializer;
    private CoordinatorResolver coordinatorResolver;
    private Coordinator coordinator;

    private final ConcurrentMap<String, GroupMetadataManager> groupMetadataManagerMap = Maps.newConcurrentMap();
    private final ConcurrentMap<String, TransactionMetadataManager> transactionMetadataManagerMap = Maps.newConcurrentMap();

    public CoordinatorService(CoordinatorConfig config, ClusterManager clusterManager, NameService nameService, BrokerTransportManager brokerTransportManager) {
        this.config = config;
        this.clusterManager = clusterManager;
        this.nameService = nameService;
        this.coordinatorInitializer = new CoordinatorInitializer(config, clusterManager, nameService);
        this.coordinatorResolver = new CoordinatorResolver(config, clusterManager);
        this.coordinator = new Coordinator(config, clusterManager, coordinatorResolver, coordinatorInitializer, brokerTransportManager);
    }

    @Override
    protected void doStart() throws Exception {
        coordinatorInitializer.init();
        LOG.info("Coordinator service started");
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