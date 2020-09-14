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
package org.joyqueue.nsr.sql;

import org.joyqueue.monitor.PointTracer;
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
import org.joyqueue.nsr.sql.operator.SQLOperator;
import org.joyqueue.nsr.sql.repository.AppTokenRepository;
import org.joyqueue.nsr.sql.repository.BaseRepository;
import org.joyqueue.nsr.sql.repository.BrokerRepository;
import org.joyqueue.nsr.sql.repository.ConfigRepository;
import org.joyqueue.nsr.sql.repository.ConsumerRepository;
import org.joyqueue.nsr.sql.repository.DataCenterRepository;
import org.joyqueue.nsr.sql.repository.NamespaceRepository;
import org.joyqueue.nsr.sql.repository.PartitionGroupReplicaRepository;
import org.joyqueue.nsr.sql.repository.PartitionGroupRepository;
import org.joyqueue.nsr.sql.repository.ProducerRepository;
import org.joyqueue.nsr.sql.repository.TopicRepository;
import org.joyqueue.nsr.sql.service.SQLAppTokenInternalService;
import org.joyqueue.nsr.sql.service.SQLBrokerInternalService;
import org.joyqueue.nsr.sql.service.SQLClusterInternalService;
import org.joyqueue.nsr.sql.service.SQLConfigInternalService;
import org.joyqueue.nsr.sql.service.SQLConsumerInternalService;
import org.joyqueue.nsr.sql.service.SQLDataCenterInternalService;
import org.joyqueue.nsr.sql.service.SQLNamespaceInternalService;
import org.joyqueue.nsr.sql.service.SQLOperationInternalService;
import org.joyqueue.nsr.sql.service.SQLPartitionGroupInternalService;
import org.joyqueue.nsr.sql.service.SQLPartitionGroupReplicaInternalService;
import org.joyqueue.nsr.sql.service.SQLProducerInternalService;
import org.joyqueue.nsr.sql.service.SQLTopicInternalService;
import org.joyqueue.nsr.sql.service.SQLTransactionInternalService;
import org.joyqueue.toolkit.service.Service;

/**
 * SQLInternalServiceManager
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class SQLInternalServiceManager extends Service {

    private SQLOperator sqlOperator;

    private BaseRepository baseRepository;
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
    private PointTracer tracer;

    private SQLTopicInternalService sqlTopicInternalService;
    private SQLPartitionGroupInternalService sqlPartitionGroupInternalService;
    private SQLPartitionGroupReplicaInternalService sqlPartitionGroupReplicaInternalService;
    private SQLBrokerInternalService sqlBrokerInternalService;
    private SQLConsumerInternalService sqlConsumerInternalService;
    private SQLProducerInternalService sqlProducerInternalService;
    private SQLDataCenterInternalService sqlDataCenterInternalService;
    private SQLNamespaceInternalService sqlNamespaceInternalService;
    private SQLConfigInternalService sqlConfigInternalService;
    private SQLAppTokenInternalService sqlAppTokenInternalService;
    private SQLTransactionInternalService sqlTransactionInternalService;
    private SQLOperationInternalService sqlOperationInternalService;
    private SQLClusterInternalService sqlClusterInternalService;

    public SQLInternalServiceManager(SQLOperator sqlOperator, PointTracer tracer) {
        this.sqlOperator = sqlOperator;
        this.tracer = tracer;
    }

    @Override
    protected void validate() throws Exception {
        baseRepository = createBaseRepository(sqlOperator, tracer);
        topicRepository = new TopicRepository(baseRepository);
        partitionGroupRepository = new PartitionGroupRepository(baseRepository);
        partitionGroupReplicaRepository = new PartitionGroupReplicaRepository(baseRepository);
        brokerRepository = new BrokerRepository(baseRepository);
        consumerRepository = new ConsumerRepository(baseRepository);
        producerRepository = new ProducerRepository(baseRepository);
        dataCenterRepository = new DataCenterRepository(baseRepository);
        namespaceRepository = new NamespaceRepository(baseRepository);
        configRepository = new ConfigRepository(baseRepository);
        appTokenRepository = new AppTokenRepository(baseRepository);

        sqlTopicInternalService = new SQLTopicInternalService(topicRepository, partitionGroupRepository, partitionGroupReplicaRepository);
        sqlPartitionGroupInternalService = new SQLPartitionGroupInternalService(partitionGroupRepository);
        sqlPartitionGroupReplicaInternalService = new SQLPartitionGroupReplicaInternalService(partitionGroupReplicaRepository);
        sqlBrokerInternalService = new SQLBrokerInternalService(brokerRepository);
        sqlConsumerInternalService = new SQLConsumerInternalService(consumerRepository);
        sqlProducerInternalService = new SQLProducerInternalService(producerRepository);
        sqlDataCenterInternalService = new SQLDataCenterInternalService(dataCenterRepository);
        sqlNamespaceInternalService = new SQLNamespaceInternalService(namespaceRepository);
        sqlConfigInternalService = new SQLConfigInternalService(configRepository);
        sqlAppTokenInternalService = new SQLAppTokenInternalService(appTokenRepository);
        sqlTransactionInternalService = new SQLTransactionInternalService();
        sqlOperationInternalService = new SQLOperationInternalService(baseRepository);
        sqlClusterInternalService = new SQLClusterInternalService();
    }

    protected BaseRepository createBaseRepository(SQLOperator sqlOperator, PointTracer tracer) {
        return new BaseRepository(sqlOperator, tracer);
    }

    public <T> T getService(Class<T> service) {
        if (service.equals(TopicInternalService.class)) {
            return (T) sqlTopicInternalService;
        } else if (service.equals(PartitionGroupInternalService.class)) {
            return (T) sqlPartitionGroupInternalService;
        } else if (service.equals(PartitionGroupReplicaInternalService.class)) {
            return (T) sqlPartitionGroupReplicaInternalService;
        } else if (service.equals(BrokerInternalService.class)) {
            return (T) sqlBrokerInternalService;
        } else if (service.equals(ConsumerInternalService.class)) {
            return (T) sqlConsumerInternalService;
        } else if (service.equals(ProducerInternalService.class)) {
            return (T) sqlProducerInternalService;
        } else if (service.equals(DataCenterInternalService.class)) {
            return (T) sqlDataCenterInternalService;
        } else if (service.equals(NamespaceInternalService.class)) {
            return (T) sqlNamespaceInternalService;
        } else if (service.equals(ConfigInternalService.class)) {
            return (T) sqlConfigInternalService;
        } else if (service.equals(AppTokenInternalService.class)) {
            return (T) sqlAppTokenInternalService;
        } else if (service.equals(TransactionInternalService.class)) {
            return (T) sqlTransactionInternalService;
        } else if (service.equals(OperationInternalService.class)) {
            return (T) sqlOperationInternalService;
        } else if (service.equals(ClusterInternalService.class)) {
            return (T) sqlClusterInternalService;
        }
        throw new UnsupportedOperationException(service.getName());
    }
}