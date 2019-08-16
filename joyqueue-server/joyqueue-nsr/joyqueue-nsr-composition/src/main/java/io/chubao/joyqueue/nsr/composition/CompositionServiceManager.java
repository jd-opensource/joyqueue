package io.chubao.joyqueue.nsr.composition;

import io.chubao.joyqueue.nsr.ServiceProvider;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.composition.service.CompositionAppTokenService;
import io.chubao.joyqueue.nsr.composition.service.CompositionBrokerService;
import io.chubao.joyqueue.nsr.composition.service.CompositionConfigService;
import io.chubao.joyqueue.nsr.composition.service.CompositionConsumerService;
import io.chubao.joyqueue.nsr.composition.service.CompositionDataCenterService;
import io.chubao.joyqueue.nsr.composition.service.CompositionNamespaceService;
import io.chubao.joyqueue.nsr.composition.service.CompositionPartitionGroupReplicaService;
import io.chubao.joyqueue.nsr.composition.service.CompositionPartitionGroupService;
import io.chubao.joyqueue.nsr.composition.service.CompositionProducerService;
import io.chubao.joyqueue.nsr.composition.service.CompositionTopicService;
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

/**
 * CompositionServiceManager
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class CompositionServiceManager extends Service {

    private CompositionConfig config;
    private ServiceProvider serviceProvider;
    private ServiceProvider igniteServiceProvider;
    private ServiceProvider journalkeeperServiceProvider;

    private CompositionAppTokenService compositionAppTokenService;
    private CompositionBrokerService compositionBrokerService;
    private CompositionConfigService compositionConfigService;
    private CompositionConsumerService compositionConsumerService;
    private CompositionDataCenterService compositionDataCenterService;
    private CompositionNamespaceService compositionNamespaceService;
    private CompositionPartitionGroupService compositionPartitionGroupService;
    private CompositionPartitionGroupReplicaService compositionPartitionGroupReplicaService;
    private CompositionProducerService compositionProducerService;
    private CompositionTopicService compositionTopicService;

    public CompositionServiceManager(CompositionConfig config, ServiceProvider serviceProvider, ServiceProvider igniteServiceProvider,
                                     ServiceProvider journalkeeperServiceProvider) {
        this.config = config;
        this.serviceProvider = serviceProvider;
        this.igniteServiceProvider = igniteServiceProvider;
        this.journalkeeperServiceProvider = journalkeeperServiceProvider;
    }

    @Override
    protected void validate() throws Exception {
        compositionAppTokenService = new CompositionAppTokenService(config, igniteServiceProvider.getService(AppTokenService.class),
                journalkeeperServiceProvider.getService(AppTokenService.class));
        compositionBrokerService = new CompositionBrokerService(config, igniteServiceProvider.getService(BrokerService.class),
                journalkeeperServiceProvider.getService(BrokerService.class));
        compositionConfigService = new CompositionConfigService(config, igniteServiceProvider.getService(ConfigService.class),
                journalkeeperServiceProvider.getService(ConfigService.class));
        compositionConsumerService = new CompositionConsumerService(config, igniteServiceProvider.getService(ConsumerService.class),
                journalkeeperServiceProvider.getService(ConsumerService.class));
        compositionDataCenterService = new CompositionDataCenterService(config, igniteServiceProvider.getService(DataCenterService.class),
                journalkeeperServiceProvider.getService(DataCenterService.class));
        compositionNamespaceService = new CompositionNamespaceService(config, igniteServiceProvider.getService(NamespaceService.class),
                journalkeeperServiceProvider.getService(NamespaceService.class));
        compositionPartitionGroupService = new CompositionPartitionGroupService(config, igniteServiceProvider.getService(PartitionGroupService.class),
                journalkeeperServiceProvider.getService(PartitionGroupService.class));
        compositionPartitionGroupReplicaService = new CompositionPartitionGroupReplicaService(config, igniteServiceProvider.getService(PartitionGroupReplicaService.class),
                journalkeeperServiceProvider.getService(PartitionGroupReplicaService.class));
        compositionProducerService = new CompositionProducerService(config, igniteServiceProvider.getService(ProducerService.class),
                journalkeeperServiceProvider.getService(ProducerService.class));
        compositionTopicService = new CompositionTopicService(config, igniteServiceProvider.getService(TopicService.class),
                journalkeeperServiceProvider.getService(TopicService.class));
    }

    public <T> T getService(Class<T> service) {
        if (service.equals(AppTokenService.class)) {
            return (T) compositionAppTokenService;
        } else if (service.equals(BrokerService.class)) {
            return (T) compositionBrokerService;
        } else if (service.equals(ConfigService.class)) {
            return (T) compositionConfigService;
        } else if (service.equals(ConsumerService.class)) {
            return (T) compositionConsumerService;
        } else if (service.equals(DataCenterService.class)) {
            return (T) compositionDataCenterService;
        } else if (service.equals(NamespaceService.class)) {
            return (T) compositionNamespaceService;
        } else if (service.equals(PartitionGroupService.class)) {
            return (T) compositionPartitionGroupService;
        } else if (service.equals(PartitionGroupReplicaService.class)) {
            return (T) compositionPartitionGroupReplicaService;
        } else if (service.equals(ProducerService.class)) {
            return (T) compositionProducerService;
        } else if (service.equals(TopicService.class)) {
            return (T) compositionTopicService;
        } else if (service.equals(Messenger.class)) {
            if (igniteServiceProvider != null) {
                return igniteServiceProvider.getService((Class<T>) Messenger.class);
            } else if (journalkeeperServiceProvider != null) {
                return journalkeeperServiceProvider.getService((Class<T>) Messenger.class);
            } else if (serviceProvider != null) {
                return serviceProvider.getService((Class<T>) Messenger.class);
            }
        }
        throw new UnsupportedOperationException(service.getName());
    }
}