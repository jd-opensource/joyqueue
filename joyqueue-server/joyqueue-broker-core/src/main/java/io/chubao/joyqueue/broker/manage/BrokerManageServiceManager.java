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
package io.chubao.joyqueue.broker.manage;

import io.chubao.joyqueue.broker.archive.ArchiveManager;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.consumer.MessageConvertSupport;
import io.chubao.joyqueue.broker.coordinator.CoordinatorService;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.broker.manage.service.BrokerManageService;
import io.chubao.joyqueue.broker.manage.service.ConnectionManageService;
import io.chubao.joyqueue.broker.manage.service.support.DefaultBrokerManageService;
import io.chubao.joyqueue.broker.manage.service.support.DefaultConnectionManageService;
import io.chubao.joyqueue.broker.manage.service.support.DefaultConsumerManageService;
import io.chubao.joyqueue.broker.manage.service.support.DefaultCoordinatorManageService;
import io.chubao.joyqueue.broker.manage.service.support.DefaultElectionManageService;
import io.chubao.joyqueue.broker.manage.service.support.DefaultMessageManageService;
import io.chubao.joyqueue.broker.manage.service.support.DefaultStoreManageService;
import io.chubao.joyqueue.broker.monitor.BrokerMonitor;
import io.chubao.joyqueue.broker.monitor.service.BrokerMonitorService;
import io.chubao.joyqueue.broker.monitor.service.support.*;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStat;
import io.chubao.joyqueue.monitor.BrokerStartupInfo;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.server.retry.api.MessageRetry;
import io.chubao.joyqueue.store.StoreManagementService;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.toolkit.service.Service;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * BrokerManageServiceManager
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class BrokerManageServiceManager extends Service {

    private BrokerMonitor brokerMonitor;
    private ClusterManager clusterManager;
    private StoreManagementService storeManagementService;
    private StoreService storeService;
    private Consume consume;
    private BrokerMonitorService brokerMonitorService;
    private BrokerManageService brokerManageService;
    private MessageRetry retryManager;
    private CoordinatorService coordinatorService;
    private ArchiveManager archiveManager;
    private NameService nameService;
    private ElectionService electionManager;
    private MessageConvertSupport messageConvertSupport;

    public BrokerManageServiceManager(BrokerMonitor brokerMonitor, ClusterManager clusterManager,
                                      StoreManagementService storeManagementService,
                                      StoreService storeService, Consume consume,
                                      MessageRetry messageRetry, CoordinatorService coordinatorService,
                                      ArchiveManager archiveManager, NameService nameService, ElectionService electionManager,
                                      MessageConvertSupport messageConvertSupport) {
        this.brokerMonitor = brokerMonitor;
        this.clusterManager = clusterManager;
        this.storeManagementService = storeManagementService;
        this.storeService = storeService;
        this.consume = consume;
        this.retryManager = messageRetry;
        this.coordinatorService = coordinatorService;
        this.archiveManager = archiveManager;
        this.nameService = nameService;
        this.electionManager = electionManager;
        this.messageConvertSupport = messageConvertSupport;
    }

    @Override
    protected void validate() throws Exception {
        this.brokerMonitorService = newBrokerMonitorService();
        this.brokerManageService = newBrokerManageService();
    }

    protected BrokerMonitorService newBrokerMonitorService() {
        BrokerStat brokerStat = brokerMonitor.getBrokerStat();
        BrokerStartupInfo brokerStartupInfo = newBrokerStartInfo();
        DefaultBrokerMonitorInternalService brokerMonitorInternalService = new DefaultBrokerMonitorInternalService(brokerStat, consume,
                storeManagementService, nameService, storeService, electionManager, clusterManager, brokerStartupInfo);
        DefaultConnectionMonitorService connectionMonitorService = new DefaultConnectionMonitorService(brokerStat);
        DefaultConsumerMonitorService consumerMonitorService = new DefaultConsumerMonitorService(brokerStat, consume, storeManagementService, retryManager, clusterManager);
        DefaultProducerMonitorService producerMonitorService = new DefaultProducerMonitorService(brokerStat, storeManagementService, clusterManager);
        DefaultTopicMonitorService topicMonitorService = new DefaultTopicMonitorService(brokerStat,storeManagementService);
        DefaultPartitionMonitorService partitionMonitorService = new DefaultPartitionMonitorService(brokerStat, storeManagementService);
        DefaultCoordinatorMonitorService coordinatorMonitorService = new DefaultCoordinatorMonitorService(coordinatorService);
        DefaultArchiveMonitorService archiveMonitorService = new DefaultArchiveMonitorService(archiveManager);
        DefaultMetadataMonitorService metadataMonitorService = new DefaultMetadataMonitorService(clusterManager);
        return new DefaultBrokerMonitorService(brokerMonitorInternalService, connectionMonitorService,
                consumerMonitorService, producerMonitorService,
                topicMonitorService, partitionMonitorService,
                coordinatorMonitorService, archiveMonitorService,
                metadataMonitorService);
    }

    protected BrokerManageService newBrokerManageService() {
        ConnectionManageService connectionManageService = new DefaultConnectionManageService(brokerMonitor.getSessionManager());
        DefaultMessageManageService messageManageService = new DefaultMessageManageService(consume, storeManagementService, messageConvertSupport);
        DefaultStoreManageService storeManageService = new DefaultStoreManageService(storeManagementService);
        DefaultConsumerManageService consumerManageService = new DefaultConsumerManageService(consume, storeManagementService, storeService, clusterManager);
        DefaultCoordinatorManageService coordinatorManageService = new DefaultCoordinatorManageService(coordinatorService);
        DefaultElectionManageService electionManageService = new DefaultElectionManageService(electionManager);
        return new DefaultBrokerManageService(connectionManageService, messageManageService, storeManageService, consumerManageService, coordinatorManageService, electionManageService);
    }

    protected BrokerStartupInfo newBrokerStartInfo() {
        BrokerStartupInfo brokerStartupInfo = new BrokerStartupInfo();
        brokerStartupInfo.setStartupTime(new Date().getTime());

        String revision = null;
        String commitDate = null;
        try (InputStream propFile = BrokerManageServiceManager.class.getClassLoader().getResourceAsStream(".version.properties")) {
            if (propFile != null) {
                Properties properties = new Properties();
                properties.load(propFile);
                String propRevision = properties.getProperty("git.commit.id.abbrev");
                String propCommitDate = properties.getProperty("git.commit.time");
                revision = propRevision != null ? propRevision : "UNKNOWN";
                commitDate = propCommitDate != null ? propCommitDate : "UNKNOWN";
            }
        } catch (Throwable t) {

        }
        brokerStartupInfo.setCommitDate(commitDate);
        brokerStartupInfo.setRevision(revision);
        return brokerStartupInfo;
    }
    public BrokerManageService getBrokerManageService() {
        return brokerManageService;
    }

    public BrokerMonitorService getBrokerMonitorService() {
        return brokerMonitorService;
    }
}