/**
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
package com.jd.joyqueue.nsr.network;

import com.jd.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.joyqueue.nsr.NameService;
import com.jd.joyqueue.nsr.NameServiceAware;
import com.jd.joyqueue.nsr.NsrPlugins;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
abstract class NsrCommandHandlerFactory extends DefaultCommandHandlerFactory {
    public abstract String getType();

    public abstract void doWithHandler(NsrCommandHandler nsrCommandHandler);

    public void register(NameService nameService) {
        NsrPlugins.nsrCommandHandlerPlugins.metas(getType()).forEach(meta -> {
            NsrCommandHandler commandHandler = meta.getTarget();
            enrichIfNecessary(commandHandler, nameService);
            register(commandHandler);
            doWithHandler(meta.getTarget());
        });
    }

    public static void enrichIfNecessary(Object obj, NameService nameService) {
        if (obj == null) {
            return;
        }
        if (obj instanceof NameServiceAware) {
            ((NameServiceAware) obj).setNameService(nameService);
        }
    }
}
