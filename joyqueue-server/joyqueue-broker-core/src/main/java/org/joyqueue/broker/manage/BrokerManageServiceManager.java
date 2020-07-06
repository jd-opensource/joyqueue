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
import org.joyqueue.broker.cluster.ClusterNameService;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.MessageConvertSupport;
import org.joyqueue.broker.coordinator.CoordinatorService;
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.broker.manage.service.BrokerManageService;
import org.joyqueue.broker.manage.service.ConnectionManageService;
import org.joyqueue.broker.manage.service.support.DefaultBrokerManageService;
import org.joyqueue.broker.manage.service.support.DefaultConnectionManageService;
import org.joyqueue.broker.manage.service.support.DefaultConsumerManageService;
import org.joyqueue.broker.manage.service.support.DefaultCoordinatorManageService;
import org.joyqueue.broker.manage.service.support.DefaultElectionManageService;
import org.joyqueue.broker.manage.service.support.DefaultMessageManageService;
import org.joyqueue.broker.manage.service.support.DefaultStoreManageService;
import org.joyqueue.broker.monitor.BrokerMonitor;
import org.joyqueue.broker.monitor.service.BrokerMonitorService;
import org.joyqueue.broker.monitor.service.support.DefaultArchiveMonitorService;
import org.joyqueue.broker.monitor.service.support.DefaultBrokerMonitorInternalService;
import org.joyqueue.broker.monitor.service.support.DefaultBrokerMonitorService;
import org.joyqueue.broker.monitor.service.support.DefaultConnectionMonitorService;
import org.joyqueue.broker.monitor.service.support.DefaultConsumerMonitorService;
import org.joyqueue.broker.monitor.service.support.DefaultCoordinatorMonitorService;
import org.joyqueue.broker.monitor.service.support.DefaultMetadataMonitorService;
import org.joyqueue.broker.monitor.service.support.DefaultPartitionMonitorService;
import org.joyqueue.broker.monitor.service.support.DefaultProducerMonitorService;
import org.joyqueue.broker.monitor.service.support.DefaultTopicMonitorService;
import org.joyqueue.broker.monitor.stat.BrokerStat;
import org.joyqueue.monitor.BrokerStartupInfo;
import org.joyqueue.nsr.NameService;
import org.joyqueue.server.retry.api.MessageRetry;
import org.joyqueue.store.StoreManagementService;
import org.joyqueue.store.StoreService;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;

import java.io.InputStream;
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
    private ClusterNameService clusterNameService;
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
                                      ClusterNameService clusterNameService, StoreManagementService storeManagementService,
                                      StoreService storeService, Consume consume,
                                      MessageRetry messageRetry, CoordinatorService coordinatorService,
                                      ArchiveManager archiveManager, NameService nameService, ElectionService electionManager,
                                      MessageConvertSupport messageConvertSupport) {
        this.brokerMonitor = brokerMonitor;
        this.clusterManager = clusterManager;
        this.clusterNameService = clusterNameService;
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
                storeManagementService, nameService, storeService, electionManager, clusterManager, brokerStartupInfo,archiveManager);
        DefaultConnectionMonitorService connectionMonitorService = new DefaultConnectionMonitorService(brokerStat);
        DefaultConsumerMonitorService consumerMonitorService = new DefaultConsumerMonitorService(brokerStat, consume, storeManagementService, retryManager, clusterManager);
        DefaultProducerMonitorService producerMonitorService = new DefaultProducerMonitorService(brokerStat, storeManagementService, clusterManager);
        DefaultTopicMonitorService topicMonitorService = new DefaultTopicMonitorService(brokerStat,storeManagementService);
        DefaultPartitionMonitorService partitionMonitorService = new DefaultPartitionMonitorService(brokerStat, storeManagementService,electionManager);
        DefaultCoordinatorMonitorService coordinatorMonitorService = new DefaultCoordinatorMonitorService(coordinatorService);
        DefaultArchiveMonitorService archiveMonitorService = new DefaultArchiveMonitorService(archiveManager);
        DefaultMetadataMonitorService metadataMonitorService = new DefaultMetadataMonitorService(clusterManager, clusterNameService);
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
        DefaultConsumerManageService consumerManageService = new DefaultConsumerManageService(consume, storeManagementService, storeService, clusterManager,brokerMonitor);
        DefaultCoordinatorManageService coordinatorManageService = new DefaultCoordinatorManageService(coordinatorService);
        DefaultElectionManageService electionManageService = new DefaultElectionManageService(electionManager);
        return new DefaultBrokerManageService(connectionManageService, messageManageService, storeManageService, consumerManageService, coordinatorManageService, electionManageService);
    }

    protected BrokerStartupInfo newBrokerStartInfo() {
        BrokerStartupInfo brokerStartupInfo = new BrokerStartupInfo();
        brokerStartupInfo.setStartupTime(SystemClock.now());

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

        try (InputStream propFile = BrokerManageServiceManager.class.getClassLoader().getResourceAsStream("joyqueue/version.properties")) {
            if (propFile != null) {
                Properties properties = new Properties();
                properties.load(propFile);
                brokerStartupInfo.setVersion(properties.getProperty("version"));
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