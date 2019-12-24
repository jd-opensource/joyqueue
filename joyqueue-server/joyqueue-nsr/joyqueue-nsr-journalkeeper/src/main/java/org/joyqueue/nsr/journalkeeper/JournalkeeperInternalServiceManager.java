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
package org.joyqueue.nsr.journalkeeper;

import org.joyqueue.nsr.journalkeeper.repository.AppTokenRepository;
import org.joyqueue.nsr.journalkeeper.repository.BaseRepository;
import org.joyqueue.nsr.journalkeeper.repository.BrokerRepository;
import org.joyqueue.nsr.journalkeeper.repository.ConfigRepository;
import org.joyqueue.nsr.journalkeeper.repository.ConsumerRepository;
import org.joyqueue.nsr.journalkeeper.repository.DataCenterRepository;
import org.joyqueue.nsr.journalkeeper.repository.NamespaceRepository;
import org.joyqueue.nsr.journalkeeper.repository.PartitionGroupReplicaRepository;
import org.joyqueue.nsr.journalkeeper.repository.PartitionGroupRepository;
import org.joyqueue.nsr.journalkeeper.repository.ProducerRepository;
import org.joyqueue.nsr.journalkeeper.repository.TopicRepository;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperAppTokenInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperBrokerInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperClusterInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperConfigInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperConsumerInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperDataCenterInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperNamespaceInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperOperationInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperPartitionGroupInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperPartitionGroupReplicaInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperProducerInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperTopicInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperTransactionInternalService;
import org.joyqueue.nsr.service.internal.AppTokenInternalService;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.joyqueue.nsr.service.internal.ClusterInternalService;
import org.joyqueue.nsr.service.internal.ConfigInternalService;
import org.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.joyqueue.nsr.service.internal.DataCenterInternalService;
import org.joyqueue.nsr.service.internal.NamespaceInternalService;
import org.joyqueue.nsr.service.internal.OperationInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import org.joyqueue.nsr.service.internal.ProducerInternalService;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
import org.joyqueue.toolkit.service.Service;
import io.journalkeeper.sql.client.SQLClient;
import io.journalkeeper.sql.client.SQLOperator;
import io.journalkeeper.sql.server.SQLServer;

/**
 * JournalkeeperInternalServiceManager
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class JournalkeeperInternalServiceManager extends Service {

    private SQLServer sqlServer;
    private SQLClient sqlClient;
    private SQLOperator sqlOperator;

    private TopicRepository topicRepository;
    private PartitionGroupRepository partitionGroupRepository;
    private PartitionGroupReplicaRepository partitionGroupReplicaRepository;
    private BrokerRepository brokerRepository;
    private ConsumerRepository consumerRepository;
    private ProducerRepository producerRepository;
    private DataCenterRepository dataCenterRepository;
    private NamespaceRepository namespaceRepository;
    private ConfigRepository configRepository;
    private AppTokenRepository appTokenRepository;

    private JournalkeeperTopicInternalService journalkeeperTopicInternalService;
    private JournalkeeperPartitionGroupInternalService journalkeeperPartitionGroupInternalService;
    private JournalkeeperPartitionGroupReplicaInternalService journalkeeperPartitionGroupReplicaInternalService;
    private JournalkeeperBrokerInternalService journalkeeperBrokerInternalService;
    private JournalkeeperConsumerInternalService journalkeeperConsumerInternalService;
    private JournalkeeperProducerInternalService journalkeeperProducerInternalService;
    private JournalkeeperDataCenterInternalService journalkeeperDataCenterInternalService;
    private JournalkeeperNamespaceInternalService journalkeeperNamespaceInternalService;
    private JournalkeeperConfigInternalService journalkeeperConfigInternalService;
    private JournalkeeperAppTokenInternalService journalkeeperAppTokenInternalService;
    private JournalkeeperTransactionInternalService journalkeeperTransactionInternalService;
    private JournalkeeperOperationInternalService journalkeeperOperationInternalService;
    private JournalkeeperClusterInternalService journalkeeperClusterInternalService;

    public JournalkeeperInternalServiceManager(SQLServer sqlServer, SQLClient sqlClient, SQLOperator sqlOperator) {
        this.sqlServer = sqlServer;
        this.sqlClient = sqlClient;
        this.sqlOperator = sqlOperator;
    }

    @Override
    protected void validate() throws Exception {
        topicRepository = new TopicRepository(sqlOperator);
        partitionGroupRepository = new PartitionGroupRepository(sqlOperator);
        partitionGroupReplicaRepository = new PartitionGroupReplicaRepository(sqlOperator);
        brokerRepository = new BrokerRepository(sqlOperator);
        consumerRepository = new ConsumerRepository(sqlOperator);
        producerRepository = new ProducerRepository(sqlOperator);
        dataCenterRepository = new DataCenterRepository(sqlOperator);
        namespaceRepository = new NamespaceRepository(sqlOperator);
        configRepository = new ConfigRepository(sqlOperator);
        appTokenRepository = new AppTokenRepository(sqlOperator);

        journalkeeperTopicInternalService = new JournalkeeperTopicInternalService(topicRepository, partitionGroupRepository, partitionGroupReplicaRepository);
        journalkeeperPartitionGroupInternalService = new JournalkeeperPartitionGroupInternalService(partitionGroupRepository);
        journalkeeperPartitionGroupReplicaInternalService = new JournalkeeperPartitionGroupReplicaInternalService(partitionGroupReplicaRepository);
        journalkeeperBrokerInternalService = new JournalkeeperBrokerInternalService(brokerRepository);
        journalkeeperConsumerInternalService = new JournalkeeperConsumerInternalService(consumerRepository);
        journalkeeperProducerInternalService = new JournalkeeperProducerInternalService(producerRepository);
        journalkeeperDataCenterInternalService = new JournalkeeperDataCenterInternalService(dataCenterRepository);
        journalkeeperNamespaceInternalService = new JournalkeeperNamespaceInternalService(namespaceRepository);
        journalkeeperConfigInternalService = new JournalkeeperConfigInternalService(configRepository);
        journalkeeperAppTokenInternalService = new JournalkeeperAppTokenInternalService(appTokenRepository);
        journalkeeperTransactionInternalService = new JournalkeeperTransactionInternalService();
        journalkeeperOperationInternalService = new JournalkeeperOperationInternalService(new BaseRepository(sqlOperator));
        journalkeeperClusterInternalService = new JournalkeeperClusterInternalService(sqlClient);
    }

    public <T> T getService(Class<T> service) {
        if (service.equals(TopicInternalService.class)) {
            return (T) journalkeeperTopicInternalService;
        } else if (service.equals(PartitionGroupInternalService.class)) {
            return (T) journalkeeperPartitionGroupInternalService;
        } else if (service.equals(PartitionGroupReplicaInternalService.class)) {
            return (T) journalkeeperPartitionGroupReplicaInternalService;
        } else if (service.equals(BrokerInternalService.class)) {
            return (T) journalkeeperBrokerInternalService;
        } else if (service.equals(ConsumerInternalService.class)) {
            return (T) journalkeeperConsumerInternalService;
        } else if (service.equals(ProducerInternalService.class)) {
            return (T) journalkeeperProducerInternalService;
        } else if (service.equals(DataCenterInternalService.class)) {
            return (T) journalkeeperDataCenterInternalService;
        } else if (service.equals(NamespaceInternalService.class)) {
            return (T) journalkeeperNamespaceInternalService;
        } else if (service.equals(ConfigInternalService.class)) {
            return (T) journalkeeperConfigInternalService;
        } else if (service.equals(AppTokenInternalService.class)) {
            return (T) journalkeeperAppTokenInternalService;
        } else if (service.equals(TransactionInternalService.class)) {
            return (T) journalkeeperTransactionInternalService;
        } else if (service.equals(OperationInternalService.class)) {
            return (T) journalkeeperOperationInternalService;
        } else if (service.equals(ClusterInternalService.class)) {
            return (T) journalkeeperClusterInternalService;
        }
        throw new UnsupportedOperationException(service.getName());
    }
}