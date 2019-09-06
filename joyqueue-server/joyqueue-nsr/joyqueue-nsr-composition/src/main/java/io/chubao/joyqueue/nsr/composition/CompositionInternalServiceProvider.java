package io.chubao.joyqueue.nsr.composition;

import com.google.common.base.Preconditions;
import com.jd.laf.extension.Extension;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import io.chubao.joyqueue.nsr.InternalServiceProvider;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;
import io.chubao.joyqueue.toolkit.service.Service;

/**
 * CompositionInternalServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/12
 */
@Extension(order = 0)
public class CompositionInternalServiceProvider extends Service implements InternalServiceProvider, PropertySupplierAware {

    private static final ExtensionPoint<InternalServiceProvider, String> SERVICE_PROVIDER_POINT = new ExtensionPointLazy<>(InternalServiceProvider.class);

    private PropertySupplier propertySupplier;
    private InternalServiceProvider serviceProvider;
    private InternalServiceProvider igniteServiceProvider;
    private InternalServiceProvider journalkeeperServiceProvider;
    private CompositionInternalServiceManager compositionInternalServiceManager;

    private CompositionConfig config;

    @Override
    public void setSupplier(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
        this.config = new CompositionConfig(propertySupplier);
    }

    @Override
    protected void validate() {
        Iterable<InternalServiceProvider> extensions = SERVICE_PROVIDER_POINT.extensions();
        for (InternalServiceProvider extension : extensions) {
            if (extension.getClass().equals(CompositionInternalServiceProvider.class)) {
                continue;
            } else if (extension.getClass().getName().contains("ignite")) {
                igniteServiceProvider = extension;
            } else if (extension.getClass().getName().contains("journalkeeper")) {
                journalkeeperServiceProvider = extension;
            }
            serviceProvider = extension;
        }
        Preconditions.checkArgument(serviceProvider != null, "serviceProvider not exist");
        this.compositionInternalServiceManager = new CompositionInternalServiceManager(config, serviceProvider, igniteServiceProvider, journalkeeperServiceProvider);
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
            compositionInternalServiceManager.start();
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
        compositionInternalServiceManager.stop();
    }

    @Override
    public <T> T getService(Class<T> service) {
        if (igniteServiceProvider != null && journalkeeperServiceProvider != null) {
            return compositionInternalServiceManager.getService(service);
        } else {
            return serviceProvider.getService(service);
        }
    }

    @Override
    public String type() {
        return "composition";
    }
}