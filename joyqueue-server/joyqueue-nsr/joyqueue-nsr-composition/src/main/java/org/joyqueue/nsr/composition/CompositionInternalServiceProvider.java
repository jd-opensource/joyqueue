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

import com.google.common.base.Preconditions;
import com.jd.laf.extension.Extension;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import org.joyqueue.nsr.InternalServiceProvider;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.lang.LifeCycle;
import org.joyqueue.toolkit.service.Service;

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