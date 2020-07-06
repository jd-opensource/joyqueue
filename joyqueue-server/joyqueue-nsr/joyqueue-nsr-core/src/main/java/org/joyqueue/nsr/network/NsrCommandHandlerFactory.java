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
package org.joyqueue.nsr.network;

import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.NameServiceAware;
import org.joyqueue.nsr.NsrPlugins;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
abstract class NsrCommandHandlerFactory extends DefaultCommandHandlerFactory {
    public abstract String getType();

    public abstract void doWithHandler(NsrCommandHandler nsrCommandHandler);

    public void register(NameService nameService, PropertySupplier propertySupplier) {
        NsrPlugins.nsrCommandHandlerPlugins.metas(getType()).forEach(meta -> {
            NsrCommandHandler commandHandler = meta.getTarget();
            enrichIfNecessary(commandHandler, nameService, propertySupplier);
            register(commandHandler);
            doWithHandler(meta.getTarget());
        });
    }

    public static void enrichIfNecessary(Object obj, NameService nameService, PropertySupplier propertySupplier) {
        if (obj == null) {
            return;
        }
        if (obj instanceof PropertySupplierAware) {
            ((PropertySupplierAware) obj).setSupplier(propertySupplier);
        }
        if (obj instanceof NameServiceAware) {
            ((NameServiceAware) obj).setNameService(nameService);
        }
    }
}
