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
package org.joyqueue.nsr.support;

import com.google.common.base.Preconditions;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import org.joyqueue.nsr.InternalServiceProvider;
import org.joyqueue.nsr.ServiceProvider;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.message.Messenger;
import org.joyqueue.nsr.service.AppTokenService;
import org.joyqueue.nsr.service.BrokerService;
import org.joyqueue.nsr.service.ConfigService;
import org.joyqueue.nsr.service.ConsumerService;
import org.joyqueue.nsr.service.DataCenterService;
import org.joyqueue.nsr.service.NamespaceService;
import org.joyqueue.nsr.service.PartitionGroupReplicaService;
import org.joyqueue.nsr.service.PartitionGroupService;
import org.joyqueue.nsr.service.ProducerService;
import org.joyqueue.nsr.service.TopicService;
import org.joyqueue.nsr.service.internal.AppTokenInternalService;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.joyqueue.nsr.service.internal.ConfigInternalService;
import org.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.joyqueue.nsr.service.internal.DataCenterInternalService;
import org.joyqueue.nsr.service.internal.NamespaceInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import org.joyqueue.nsr.service.internal.ProducerInternalService;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.lang.LifeCycle;
import org.joyqueue.toolkit.service.Service;

/**
 * DefaultServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultServiceProvider extends Service implements ServiceProvider, PropertySupplierAware {

    private ExtensionPoint<InternalServiceProvider, String> INTERNAL_SERVICE_PROVIDER_POINT = new ExtensionPointLazy<>(InternalServiceProvider.class);
    private ExtensionPoint<Messenger, String> MESSENGER_POINT = new ExtensionPointLazy<>(Messenger.class);

    private InternalServiceProvider internalServiceProvider;
    private PropertySupplier supplier;

    private AppTokenService appTokenService;
    private BrokerService brokerService;
    private ConfigService configService;
    private ConsumerService consumerService;
    private DataCenterService dataCenterService;
    private NamespaceService namespaceService;
    private PartitionGroupService partitionGroupService;
    private PartitionGroupReplicaService partitionGroupReplicaService;
    private ProducerService producerService;
    private TopicService topicService;
    private NameServiceConfig config;
    private Messenger messenger;

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.supplier = supplier;
        this.config = new NameServiceConfig(supplier);
    }

    @Override
    protected void validate() throws Exception {
        internalServiceProvider = INTERNAL_SERVICE_PROVIDER_POINT.get();
        messenger = MESSENGER_POINT.get();
        Preconditions.checkArgument(internalServiceProvider != null, "service provider can not be null.");
        Preconditions.checkArgument(messenger != null, "messenger can not be null.");
    }

    @Override
    protected void doStart() throws Exception {
        enrichIfNecessary(internalServiceProvider);
        enrichIfNecessary(messenger);

        appTokenService = new DefaultAppTokenService(internalServiceProvider.getService(AppTokenInternalService.class));
        brokerService = new DefaultBrokerService(internalServiceProvider.getService(BrokerInternalService.class), internalServiceProvider.getService(TransactionInternalService.class),
                config, messenger);
        configService = new DefaultConfigService(internalServiceProvider.getService(ConfigInternalService.class));
        consumerService = new DefaultConsumerService(internalServiceProvider.getService(TopicInternalService.class), internalServiceProvider.getService(PartitionGroupInternalService.class),
                internalServiceProvider.getService(BrokerInternalService.class), internalServiceProvider.getService(ConsumerInternalService.class),
                internalServiceProvider.getService(TransactionInternalService.class), config, messenger);
        dataCenterService = new DefaultDataCenterService(internalServiceProvider.getService(DataCenterInternalService.class));
        namespaceService = new DefaultNamespaceService(internalServiceProvider.getService(NamespaceInternalService.class));
        partitionGroupService = new DefaultPartitionGroupService(internalServiceProvider.getService(PartitionGroupInternalService.class));
        partitionGroupReplicaService = new DefaultPartitionGroupReplicaService(internalServiceProvider.getService(PartitionGroupReplicaInternalService.class));
        producerService = new DefaultProducerService(internalServiceProvider.getService(TopicInternalService.class), internalServiceProvider.getService(PartitionGroupInternalService.class),
                internalServiceProvider.getService(BrokerInternalService.class), internalServiceProvider.getService(ProducerInternalService.class),
                internalServiceProvider.getService(TransactionInternalService.class), config, messenger);
        topicService = new DefaultTopicService(config, messenger, internalServiceProvider.getService(TopicInternalService.class),
                internalServiceProvider.getService(PartitionGroupInternalService.class), internalServiceProvider.getService(BrokerInternalService.class),
                internalServiceProvider.getService(TransactionInternalService.class));
    }

    protected  <T> T enrichIfNecessary(T obj) throws Exception {
        if (obj instanceof LifeCycle) {
            if (((LifeCycle) obj).isStarted()) {
                return obj;
            }
        }
        if (obj instanceof PropertySupplierAware) {
            ((PropertySupplierAware) obj).setSupplier(supplier);
        }
        if (obj instanceof LifeCycle) {
            ((LifeCycle) obj).start();
        }
        return obj;
    }

    @Override
    public <T> T getService(Class<T> service) {
        if (service.equals(AppTokenService.class)) {
            return (T) appTokenService;
        } else if (service.equals(BrokerService.class)) {
            return (T) brokerService;
        } else if (service.equals(ConfigService.class)) {
            return (T) configService;
        } else if (service.equals(ConsumerService.class)) {
            return (T) consumerService;
        } else if (service.equals(DataCenterService.class)) {
            return (T) dataCenterService;
        } else if (service.equals(NamespaceService.class)) {
            return (T) namespaceService;
        } else if (service.equals(PartitionGroupService.class)) {
            return (T) partitionGroupService;
        } else if (service.equals(PartitionGroupReplicaService.class)) {
            return (T) partitionGroupReplicaService;
        } else if (service.equals(ProducerService.class)) {
            return (T) producerService;
        } else if (service.equals(TopicService.class)) {
            return (T) topicService;
        } else if (service.equals(Messenger.class)) {
            return (T) messenger;
        }
        throw new UnsupportedOperationException(service.getName());
    }
}