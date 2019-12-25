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
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class UnSubscribeHandler extends Handler implements ExecutorsProvider {
    private static final Logger logger = LoggerFactory.getLogger(UnSubscribeHandler.class);

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        MqttUnsubscribeMessage unSubscribeMessage = (MqttUnsubscribeMessage) message;

        mqttProtocolHandler.processUnsubscribe(client, unSubscribeMessage);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.UNSUBSCRIBE;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(UnSubscribeHandler.class);
    }
}
