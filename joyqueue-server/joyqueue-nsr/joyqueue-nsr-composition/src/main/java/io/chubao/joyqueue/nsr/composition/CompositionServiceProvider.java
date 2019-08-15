package io.chubao.joyqueue.nsr.composition;

import com.google.common.base.Preconditions;
import com.jd.laf.extension.Extension;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
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
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;
import io.chubao.joyqueue.toolkit.service.Service;

/**
 * CompositionServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/12
 */
// TODO 临时实现
@Extension(order = 0)
public class CompositionServiceProvider extends Service implements ServiceProvider, PropertySupplierAware {

    private static final ExtensionPoint<ServiceProvider, String> SERVICE_PROVIDER_POINT = new ExtensionPointLazy<>(ServiceProvider.class);

    private PropertySupplier propertySupplier;
    private ServiceProvider serviceProvider;
    private ServiceProvider igniteServiceProvider;
    private ServiceProvider journalkeeperServiceProvider;

    private CompositionConfig config;
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

    @Override
    public void setSupplier(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
        this.config = new CompositionConfig(propertySupplier);
    }

    @Override
    protected void validate() {
        Iterable<ServiceProvider> extensions = SERVICE_PROVIDER_POINT.extensions();
        for (ServiceProvider extension : extensions) {
            if (extension.getClass().equals(CompositionServiceProvider.class)) {
                continue;
            } else if (extension.getClass().getName().contains("ignite")) {
                igniteServiceProvider = extension;
            } else if (extension.getClass().getName().contains("journalkeeper")) {
                journalkeeperServiceProvider = extension;
            }
            serviceProvider = extension;
        }
        Preconditions.checkArgument(serviceProvider != null, "serviceProvider not exist");
    }

    @Override
    protected void doStart() throws Exception {
        if (igniteServiceProvider != null && journalkeeperServiceProvider != null) {
            if (igniteServiceProvider instanceof PropertySupplierAware) {
                ((PropertySupplierAware) igniteServiceProvider).setSupplier(propertySupplier);
            }
            if (igniteServiceProvider instanceof LifeCycle) {
                ((LifeCycle) igniteServiceProvider).start();
            }
            if (journalkeeperServiceProvider instanceof PropertySupplierAware) {
                ((PropertySupplierAware) journalkeeperServiceProvider).setSupplier(propertySupplier);
            }
            if (journalkeeperServiceProvider instanceof LifeCycle) {
                ((LifeCycle) journalkeeperServiceProvider).start();
            }

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
        } else {
            if (serviceProvider instanceof PropertySupplierAware) {
                ((PropertySupplierAware) serviceProvider).setSupplier(propertySupplier);
            }
            if (serviceProvider instanceof LifeCycle) {
                ((LifeCycle) serviceProvider).start();
            }
        }
    }

    @Override
    protected void doStop() {
        if (igniteServiceProvider != null && journalkeeperServiceProvider != null) {
            if (igniteServiceProvider instanceof LifeCycle) {
                ((LifeCycle) igniteServiceProvider).stop();
            }
            if (journalkeeperServiceProvider instanceof LifeCycle) {
                ((LifeCycle) journalkeeperServiceProvider).stop();
            }
        } else {
            if (serviceProvider instanceof LifeCycle) {
                ((LifeCycle) serviceProvider).stop();
            }
        }
    }

    @Override
    public <T> T getService(Class<T> clazz) {
        if (igniteServiceProvider != null && journalkeeperServiceProvider != null) {
            return doGetService(clazz);
        } else {
            return serviceProvider.getService(clazz);
        }
    }

    protected <T> T doGetService(Class<T> clazz) {
        if (clazz.equals(AppTokenService.class)) {
            return (T) compositionAppTokenService;
        } else if (clazz.equals(BrokerService.class)) {
            return (T) compositionBrokerService;
        } else if (clazz.equals(ConfigService.class)) {
            return (T) compositionConfigService;
        } else if (clazz.equals(ConsumerService.class)) {
            return (T) compositionConsumerService;
        } else if (clazz.equals(DataCenterService.class)) {
            return (T) compositionDataCenterService;
        } else if (clazz.equals(NamespaceService.class)) {
            return (T) compositionNamespaceService;
        } else if (clazz.equals(PartitionGroupService.class)) {
            return (T) compositionPartitionGroupService;
        } else if (clazz.equals(PartitionGroupReplicaService.class)) {
            return (T) compositionPartitionGroupReplicaService;
        } else if (clazz.equals(ProducerService.class)) {
            return (T) compositionProducerService;
        } else if (clazz.equals(TopicService.class)) {
            return (T) compositionTopicService;
        } else if (clazz.equals(Messenger.class)) {
            if (igniteServiceProvider != null) {
                return igniteServiceProvider.getService((Class<T>) Messenger.class);
            } else if (journalkeeperServiceProvider != null) {
                return journalkeeperServiceProvider.getService((Class<T>) Messenger.class);
            } else if (serviceProvider != null) {
                return serviceProvider.getService((Class<T>) Messenger.class);
            }
        }
        throw new UnsupportedOperationException(clazz.getName());
    }
}