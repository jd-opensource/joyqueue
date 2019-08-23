package io.chubao.joyqueue.nsr.journalkeeper;

import io.chubao.joyqueue.nsr.journalkeeper.messenger.JournalkeeperMessenger;
import io.chubao.joyqueue.nsr.journalkeeper.repository.AppTokenRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.BrokerRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ConfigRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ConsumerRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.DataCenterRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.NamespaceRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupReplicaRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ProducerRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.TopicRepository;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperAppTokenService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperBrokerService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperConfigService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperConsumerService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperDataCenterService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperNamespaceService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperPartitionGroupReplicaService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperPartitionGroupService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperProducerService;
import io.chubao.joyqueue.nsr.journalkeeper.service.JournalkeeperTopicService;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.nsr.service.AppTokenService;
import io.chubao.joyqueue.nsr.service.BrokerService;
import io.chubao.joyqueue.nsr.service.ConfigService;
import io.chubao.joyqueue.nsr.service.ConsumerService;
import io.chubao.joyqueue.nsr.service.DataCenterService;
import io.chubao.joyqueue.nsr.service.NamespaceService;
import io.chubao.joyqueue.nsr.service.PartitionGroupReplicaService;
import io.chubao.joyqueue.nsr.service.PartitionGroupService;
import io.chubao.joyqueue.nsr.service.ProducerService;
import io.chubao.joyqueue.nsr.service.TopicService;
import io.chubao.joyqueue.toolkit.service.Service;
import io.journalkeeper.sql.client.SQLClient;
import io.journalkeeper.sql.client.SQLOperator;

/**
 * JournalkeeperServiceManager
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class JournalkeeperServiceManager extends Service {

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

    private JournalkeeperTopicService journalkeeperTopicService;
    private JournalkeeperPartitionGroupService journalkeeperPartitionGroupService;
    private JournalkeeperPartitionGroupReplicaService journalkeeperPartitionGroupReplicaService;
    private JournalkeeperBrokerService journalkeeperBrokerService;
    private JournalkeeperConsumerService journalkeeperConsumerService;
    private JournalkeeperProducerService journalkeeperProducerService;
    private JournalkeeperDataCenterService journalkeeperDataCenterService;
    private JournalkeeperNamespaceService journalkeeperNamespaceService;
    private JournalkeeperConfigService journalkeeperConfigService;
    private JournalkeeperAppTokenService journalkeeperAppTokenService;

    private JournalkeeperMessenger journalkeeperMessenger;

    public JournalkeeperServiceManager(SQLClient sqlClient, SQLOperator sqlOperator) {
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

        journalkeeperTopicService = new JournalkeeperTopicService(topicRepository, partitionGroupRepository, partitionGroupReplicaRepository);
        journalkeeperPartitionGroupService = new JournalkeeperPartitionGroupService(partitionGroupRepository);
        journalkeeperPartitionGroupReplicaService = new JournalkeeperPartitionGroupReplicaService(partitionGroupReplicaRepository);
        journalkeeperBrokerService = new JournalkeeperBrokerService(brokerRepository);
        journalkeeperConsumerService = new JournalkeeperConsumerService(consumerRepository);
        journalkeeperProducerService = new JournalkeeperProducerService(producerRepository);
        journalkeeperDataCenterService = new JournalkeeperDataCenterService(dataCenterRepository);
        journalkeeperNamespaceService = new JournalkeeperNamespaceService(namespaceRepository);
        journalkeeperConfigService = new JournalkeeperConfigService(configRepository);
        journalkeeperAppTokenService = new JournalkeeperAppTokenService(appTokenRepository);

        journalkeeperMessenger = new JournalkeeperMessenger();
    }

    public <T> T getService(Class<T> service) {
        if (service.equals(TopicService.class)) {
            return (T) journalkeeperTopicService;
        } else if (service.equals(PartitionGroupService.class)) {
            return (T) journalkeeperPartitionGroupService;
        } else if (service.equals(PartitionGroupReplicaService.class)) {
            return (T) journalkeeperPartitionGroupReplicaService;
        } else if (service.equals(BrokerService.class)) {
            return (T) journalkeeperBrokerService;
        } else if (service.equals(ConsumerService.class)) {
            return (T) journalkeeperConsumerService;
        } else if (service.equals(ProducerService.class)) {
            return (T) journalkeeperProducerService;
        } else if (service.equals(DataCenterService.class)) {
            return (T) journalkeeperDataCenterService;
        } else if (service.equals(NamespaceService.class)) {
            return (T) journalkeeperNamespaceService;
        } else if (service.equals(ConfigService.class)) {
            return (T) journalkeeperConfigService;
        } else if (service.equals(AppTokenService.class)) {
            return (T) journalkeeperAppTokenService;
        } else if (service.equals(Messenger.class)) {
            return (T) journalkeeperMessenger;
        } else if (service.equals(SQLOperator.class)) {
            return (T) sqlOperator;
        }
        throw new UnsupportedOperationException(service.getName());
    }
}