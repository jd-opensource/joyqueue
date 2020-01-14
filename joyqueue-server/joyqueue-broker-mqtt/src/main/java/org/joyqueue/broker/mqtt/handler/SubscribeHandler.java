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
package org.joyqueue.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class SubscribeHandler extends Handler implements ExecutorsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SubscribeHandler.class);

    public SubscribeHandler() {

    }

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) message;

        mqttProtocolHandler.processSubscribe(client, subscribeMessage);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.SUBSCRIBE;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(SubscribeHandler.class);
    }
}
