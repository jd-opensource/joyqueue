package io.chubao.joyqueue.nsr.journalkeeper;

import io.chubao.joyqueue.nsr.journalkeeper.repository.AppTokenRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.BaseRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.BrokerRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ConfigRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ConsumerRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.DataCenterRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.NamespaceRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupReplicaRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ProducerRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.TopicRepository;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperAppTokenInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperBrokerInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperConfigInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperConsumerInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperDataCenterInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperNamespaceInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperOperationInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperPartitionGroupInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperPartitionGroupReplicaInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperProducerInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperTopicInternalService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperTransactionInternalService;
import io.chubao.joyqueue.nsr.service.internal.AppTokenInternalService;
import io.chubao.joyqueue.nsr.service.internal.BrokerInternalService;
import io.chubao.joyqueue.nsr.service.internal.ConfigInternalService;
import io.chubao.joyqueue.nsr.service.internal.ConsumerInternalService;
import io.chubao.joyqueue.nsr.service.internal.DataCenterInternalService;
import io.chubao.joyqueue.nsr.service.internal.NamespaceInternalService;
import io.chubao.joyqueue.nsr.service.internal.OperationInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import io.chubao.joyqueue.nsr.service.internal.ProducerInternalService;
import io.chubao.joyqueue.nsr.service.internal.TopicInternalService;
import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;
import io.chubao.joyqueue.toolkit.service.Service;
import io.journalkeeper.sql.client.SQLClient;
import io.journalkeeper.sql.client.SQLOperator;

/**
 * JournalkeeperInternalServiceManager
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class JournalkeeperInternalServiceManager extends Service {

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

    public JournalkeeperInternalServiceManager(SQLClient sqlClient, SQLOperator sqlOperator) {
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
        }
        throw new UnsupportedOperationException(service.getName());
    }
}