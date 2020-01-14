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
package org.joyqueue.broker.extension;

import com.google.common.collect.Lists;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.toolkit.lang.LifeCycle;
import org.joyqueue.toolkit.service.Service;

import java.util.List;

/**
 * ExtensionManager
 *
 * author: gaohaoxiang
 * date: 2018/11/13
 */
public class ExtensionManager extends Service {

    private BrokerContext brokerContext;
    private List<ExtensionService> extensionServices;

    public ExtensionManager(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.extensionServices = loadExtensionServices();
    }

    protected List<ExtensionService> loadExtensionServices() {
        return Lists.newArrayList(com.jd.laf.extension.ExtensionManager.getOrLoadExtensions(ExtensionService.class));
    }

    public void before() {
        for (ExtensionService extensionService : extensionServices) {
            extensionService.before(brokerContext);
        }
    }

    public void after() {
        for (ExtensionService extensionService : extensionServices) {
            extensionService.after(brokerContext);
        }
    }

    @Override
    protected void doStart() throws Exception {
        for (ExtensionService extensionService : extensionServices) {
            if (extensionService instanceof LifeCycle) {
                ((LifeCycle) extensionService).start();
            }
        }
    }

    @Override
    protected void doStop() {
        for (ExtensionService extensionService : extensionServices) {
            if (extensionService instanceof LifeCycle) {
                ((LifeCycle) extensionService).stop();
            }
        }
    }
}