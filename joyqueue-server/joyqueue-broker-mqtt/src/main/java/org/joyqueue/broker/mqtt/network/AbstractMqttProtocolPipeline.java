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
package org.joyqueue.broker.mqtt.network;

import io.netty.channel.ChannelInitializer;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.mqtt.config.MqttConfig;
import org.joyqueue.broker.mqtt.config.MqttContext;
import org.joyqueue.broker.mqtt.handler.MqttHandlerDispatcher;
import org.joyqueue.broker.mqtt.transport.MqttCommandInvocation;

/**
 * @author majun8
 */
public abstract class AbstractMqttProtocolPipeline extends ChannelInitializer {
    protected MqttContext mqttContext;

    public AbstractMqttProtocolPipeline(BrokerContext brokerContext) {
        this.mqttContext = new MqttContext(new MqttConfig(brokerContext.getPropertySupplier()));
    }

    protected abstract MqttCommandInvocation newMqttCommandInvocation();

    protected abstract MqttHandlerDispatcher newMqttHandlerDispatcher();
}
