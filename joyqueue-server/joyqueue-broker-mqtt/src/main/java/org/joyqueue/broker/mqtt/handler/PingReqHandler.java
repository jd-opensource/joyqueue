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

import org.joyqueue.broker.mqtt.util.NettyAttrManager;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * @author majun8
 */
public class PingReqHandler extends Handler implements ExecutorsProvider {
    private static final Logger logger = LoggerFactory.getLogger(PingReqHandler.class);

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        String clientId = NettyAttrManager.getAttrClientId(client);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("PingRequest clientId:%s", clientId));
        }
        MqttFixedHeader pingHeader = new MqttFixedHeader(
                MqttMessageType.PINGRESP,
                false,
                AT_MOST_ONCE,
                false,
                0);
        MqttMessage pingResp = new MqttMessage(pingHeader);
        client.writeAndFlush(pingResp);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.PINGREQ;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(PingReqHandler.class);
    }
}
