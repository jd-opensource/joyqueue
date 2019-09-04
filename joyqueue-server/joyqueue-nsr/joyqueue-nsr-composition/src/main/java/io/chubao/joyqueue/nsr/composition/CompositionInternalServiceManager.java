package io.chubao.joyqueue.nsr.composition;

import io.chubao.joyqueue.nsr.InternalServiceProvider;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.composition.service.CompositionAppTokenInternalService;
import io.chubao.joyqueue.nsr.composition.service.CompositionBrokerInternalService;
import io.chubao.joyqueue.nsr.composition.service.CompositionConfigInternalService;
import io.chubao.joyqueue.nsr.composition.service.CompositionConsumerInternalService;
import io.chubao.joyqueue.nsr.composition.service.CompositionDataCenterInternalService;
import io.chubao.joyqueue.nsr.composition.service.CompositionNamespaceInternalService;
import io.chubao.joyqueue.nsr.composition.service.CompositionPartitionGroupInternalService;
import io.chubao.joyqueue.nsr.composition.service.CompositionPartitionGroupReplicaInternalService;
import io.chubao.joyqueue.nsr.composition.service.CompositionProducerInternalService;
import io.chubao.joyqueue.nsr.composition.service.CompositionTopicInternalService;
import io.chubao.joyqueue.nsr.service.internal.AppTokenInternalService;
import io.chubao.joyqueue.nsr.service.internal.BrokerInternalService;
import io.chubao.joyqueue.nsr.service.internal.ConfigInternalService;
import io.chubao.joyqueue.nsr.service.internal.ConsumerInternalService;
import io.chubao.joyqueue.nsr.service.internal.DataCenterInternalService;
import io.chubao.joyqueue.nsr.service.internal.NamespaceInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import io.chubao.joyqueue.nsr.service.internal.ProducerInternalService;
import io.chubao.joyqueue.nsr.service.internal.TopicInternalService;
import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;
import io.chubao.joyqueue.toolkit.service.Service;

/**
 * CompositionInternalServiceManager
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class CompositionInternalServiceManager extends Service {

    private CompositionConfig config;
    private InternalServiceProvider serviceProvider;
    private InternalServiceProvider igniteServiceProvider;
    private InternalServiceProvider journalkeeperServiceProvider;

    private CompositionAppTokenInternalService compositionAppTokenInternalService;
    private CompositionBrokerInternalService compositionBrokerInternalService;
    private CompositionConfigInternalService compositionConfigInternalService;
    private CompositionConsumerInternalService compositionConsumerInternalService;
    private CompositionDataCenterInternalService compositionDataCenterInternalService;
    private CompositionNamespaceInternalService compositionNamespaceInternalService;
    private CompositionPartitionGroupInternalService compositionPartitionGroupInternalService;
    private CompositionPartitionGroupReplicaInternalService compositionPartitionGroupReplicaInternalService;
    private CompositionProducerInternalService compositionProducerInternalService;
    private CompositionTopicInternalService compositionTopicInternalService;
    private CompositionTransactionInternalService compositionTransactionInternalService;

    public CompositionInternalServiceManager(CompositionConfig config, InternalServiceProvider serviceProvider, InternalServiceProvider igniteServiceProvider,
                                             InternalServiceProvider journalkeeperServiceProvider) {
        this.config = config;
        this.serviceProvider = serviceProvider;
        this.igniteServiceProvider = igniteServiceProvider;
        this.journalkeeperServiceProvider = journalkeeperServiceProvider;
    }

    @Override
    protected void validate() throws Exception {
        compositionAppTokenInternalService = new CompositionAppTokenInternalService(config, igniteServiceProvider.getService(AppTokenInternalService.class),
                journalkeeperServiceProvider.getService(AppTokenInternalService.class));
        compositionBrokerInternalService = new CompositionBrokerInternalService(config, igniteServiceProvider.getService(BrokerInternalService.class),
                journalkeeperServiceProvider.getService(BrokerInternalService.class));
        compositionConfigInternalService = new CompositionConfigInternalService(config, igniteServiceProvider.getService(ConfigInternalService.class),
                journalkeeperServiceProvider.getService(ConfigInternalService.class));
        compositionConsumerInternalService = new CompositionConsumerInternalService(config, igniteServiceProvider.getService(ConsumerInternalService.class),
                journalkeeperServiceProvider.getService(ConsumerInternalService.class));
        compositionDataCenterInternalService = new CompositionDataCenterInternalService(config, igniteServiceProvider.getService(DataCenterInternalService.class),
                journalkeeperServiceProvider.getService(DataCenterInternalService.class));
        compositionNamespaceInternalService = new CompositionNamespaceInternalService(config, igniteServiceProvider.getService(NamespaceInternalService.class),
                journalkeeperServiceProvider.getService(NamespaceInternalService.class));
        compositionPartitionGroupInternalService = new CompositionPartitionGroupInternalService(config, igniteServiceProvider.getService(PartitionGroupInternalService.class),
                journalkeeperServiceProvider.getService(PartitionGroupInternalService.class));
        compositionPartitionGroupReplicaInternalService = new CompositionPartitionGroupReplicaInternalService(config, igniteServiceProvider.getService(PartitionGroupReplicaInternalService.class),
                journalkeeperServiceProvider.getService(PartitionGroupReplicaInternalService.class));
        compositionProducerInternalService = new CompositionProducerInternalService(config, igniteServiceProvider.getService(ProducerInternalService.class),
                journalkeeperServiceProvider.getService(ProducerInternalService.class));
        compositionTopicInternalService = new CompositionTopicInternalService(config, igniteServiceProvider.getService(TopicInternalService.class),
                journalkeeperServiceProvider.getService(TopicInternalService.class));
        compositionTransactionInternalService = new CompositionTransactionInternalService(config, igniteServiceProvider.getService(TransactionInternalService.class),
                journalkeeperServiceProvider.getService(TransactionInternalService.class));
    }

    public <T> T getService(Class<T> service) {
        if (service.equals(AppTokenInternalService.class)) {
            return (T) compositionAppTokenInternalService;
        } else if (service.equals(BrokerInternalService.class)) {
            return (T) compositionBrokerInternalService;
        } else if (service.equals(ConfigInternalService.class)) {
            return (T) compositionConfigInternalService;
        } else if (service.equals(ConsumerInternalService.class)) {
            return (T) compositionConsumerInternalService;
        } else if (service.equals(DataCenterInternalService.class)) {
            return (T) compositionDataCenterInternalService;
        } else if (service.equals(NamespaceInternalService.class)) {
            return (T) compositionNamespaceInternalService;
        } else if (service.equals(PartitionGroupInternalService.class)) {
            return (T) compositionPartitionGroupInternalService;
        } else if (service.equals(PartitionGroupReplicaInternalService.class)) {
            return (T) compositionPartitionGroupReplicaInternalService;
        } else if (service.equals(ProducerInternalService.class)) {
            return (T) compositionProducerInternalService;
        } else if (service.equals(TopicInternalService.class)) {
            return (T) compositionTopicInternalService;
        } else if (service.equals(TransactionInternalService.class)) {
            return (T) compositionTransactionInternalService;
        }
        throw new UnsupportedOperationException(service.getName());
    }
}