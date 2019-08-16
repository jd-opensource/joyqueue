package io.chubao.joyqueue.nsr.composition;

import com.google.common.base.Preconditions;
import com.jd.laf.extension.Extension;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import io.chubao.joyqueue.nsr.ServiceProvider;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;
import io.chubao.joyqueue.toolkit.service.Service;

/**
 * CompositionServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/12
 */
@Extension(order = 0)
public class CompositionServiceProvider extends Service implements ServiceProvider, PropertySupplierAware {

    private static final ExtensionPoint<ServiceProvider, String> SERVICE_PROVIDER_POINT = new ExtensionPointLazy<>(ServiceProvider.class);

    private PropertySupplier propertySupplier;
    private ServiceProvider serviceProvider;
    private ServiceProvider igniteServiceProvider;
    private ServiceProvider journalkeeperServiceProvider;
    private CompositionServiceManager compositionServiceManager;

    private CompositionConfig config;

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
        this.compositionServiceManager = new CompositionServiceManager(config, serviceProvider, igniteServiceProvider, journalkeeperServiceProvider);
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
            compositionServiceManager.start();
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
        compositionServiceManager.stop();
    }

    @Override
    public <T> T getService(Class<T> service) {
        if (igniteServiceProvider != null && journalkeeperServiceProvider != null) {
            return compositionServiceManager.getService(service);
        } else {
            return serviceProvider.getService(service);
        }
    }
}