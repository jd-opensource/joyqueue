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
package org.joyqueue.nsr.composition;

import org.joyqueue.nsr.InternalServiceProvider;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.composition.service.CompositionAppTokenInternalService;
import org.joyqueue.nsr.composition.service.CompositionBrokerInternalService;
import org.joyqueue.nsr.composition.service.CompositionClusterInternalService;
import org.joyqueue.nsr.composition.service.CompositionConfigInternalService;
import org.joyqueue.nsr.composition.service.CompositionConsumerInternalService;
import org.joyqueue.nsr.composition.service.CompositionDataCenterInternalService;
import org.joyqueue.nsr.composition.service.CompositionNamespaceInternalService;
import org.joyqueue.nsr.composition.service.CompositionPartitionGroupInternalService;
import org.joyqueue.nsr.composition.service.CompositionPartitionGroupReplicaInternalService;
import org.joyqueue.nsr.composition.service.CompositionProducerInternalService;
import org.joyqueue.nsr.composition.service.CompositionTopicInternalService;
import org.joyqueue.nsr.service.internal.AppTokenInternalService;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.joyqueue.nsr.service.internal.ClusterInternalService;
import org.joyqueue.nsr.service.internal.ConfigInternalService;
import org.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.joyqueue.nsr.service.internal.DataCenterInternalService;
import org.joyqueue.nsr.service.internal.NamespaceInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import org.joyqueue.nsr.service.internal.ProducerInternalService;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
import org.joyqueue.toolkit.service.Service;

/**
 * CompositionInternalServiceManager
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class CompositionInternalServiceManager extends Service {

    private CompositionConfig config;
    private InternalServiceProvider serviceProvider;
    private InternalServiceProvider sourceServiceProvider;
    private InternalServiceProvider targetServiceProvider;

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
    private CompositionClusterInternalService compositionClusterInternalService;

    public CompositionInternalServiceManager(CompositionConfig config, InternalServiceProvider serviceProvider, InternalServiceProvider sourceServiceProvider,
                                             InternalServiceProvider targetServiceProvider) {
        this.config = config;
        this.serviceProvider = serviceProvider;
        this.sourceServiceProvider = sourceServiceProvider;
        this.targetServiceProvider = targetServiceProvider;
    }

    @Override
    protected void validate() throws Exception {
        compositionAppTokenInternalService = new CompositionAppTokenInternalService(config, sourceServiceProvider.getService(AppTokenInternalService.class),
                targetServiceProvider.getService(AppTokenInternalService.class));
        compositionBrokerInternalService = new CompositionBrokerInternalService(config, sourceServiceProvider.getService(BrokerInternalService.class),
                targetServiceProvider.getService(BrokerInternalService.class));
        compositionConfigInternalService = new CompositionConfigInternalService(config, sourceServiceProvider.getService(ConfigInternalService.class),
                targetServiceProvider.getService(ConfigInternalService.class));
        compositionConsumerInternalService = new CompositionConsumerInternalService(config, sourceServiceProvider.getService(ConsumerInternalService.class),
                targetServiceProvider.getService(ConsumerInternalService.class));
        compositionDataCenterInternalService = new CompositionDataCenterInternalService(config, sourceServiceProvider.getService(DataCenterInternalService.class),
                targetServiceProvider.getService(DataCenterInternalService.class));
        compositionNamespaceInternalService = new CompositionNamespaceInternalService(config, sourceServiceProvider.getService(NamespaceInternalService.class),
                targetServiceProvider.getService(NamespaceInternalService.class));
        compositionPartitionGroupInternalService = new CompositionPartitionGroupInternalService(config, sourceServiceProvider.getService(PartitionGroupInternalService.class),
                targetServiceProvider.getService(PartitionGroupInternalService.class));
        compositionPartitionGroupReplicaInternalService = new CompositionPartitionGroupReplicaInternalService(config, sourceServiceProvider.getService(PartitionGroupReplicaInternalService.class),
                targetServiceProvider.getService(PartitionGroupReplicaInternalService.class));
        compositionProducerInternalService = new CompositionProducerInternalService(config, sourceServiceProvider.getService(ProducerInternalService.class),
                targetServiceProvider.getService(ProducerInternalService.class));
        compositionTopicInternalService = new CompositionTopicInternalService(config, sourceServiceProvider.getService(TopicInternalService.class),
                targetServiceProvider.getService(TopicInternalService.class));
        compositionTransactionInternalService = new CompositionTransactionInternalService(config, sourceServiceProvider.getService(TransactionInternalService.class),
                targetServiceProvider.getService(TransactionInternalService.class));
        compositionClusterInternalService = new CompositionClusterInternalService(config, null,
                targetServiceProvider.getService(ClusterInternalService.class));
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
        } else if (service.equals(ClusterInternalService.class)) {
            return (T) compositionClusterInternalService;
        }
        throw new UnsupportedOperationException(service.getName());
    }
}