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
package org.joyqueue.broker.mqtt.protocol;

import com.google.common.collect.Lists;
import org.joyqueue.broker.mqtt.command.MqttHandlerFactory;
import org.joyqueue.broker.mqtt.handler.Handler;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * @author majun8
 */
public class MqttHandlerRegister {
    public static CommandHandlerFactory register(MqttHandlerFactory mqttHandlerFactory) {
        List<Handler> handlers = loadCommandHandlers();
        mqttHandlerFactory.registers(handlers);
        return mqttHandlerFactory;
    }

    private static List<Handler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(Handler.class));
    }
}
