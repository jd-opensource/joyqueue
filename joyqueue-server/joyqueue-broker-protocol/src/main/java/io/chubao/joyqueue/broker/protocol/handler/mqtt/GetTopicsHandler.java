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
package io.chubao.joyqueue.broker.protocol.handler.mqtt;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.domain.Subscription;
import io.chubao.joyqueue.network.command.GetTopics;
import io.chubao.joyqueue.network.command.GetTopicsAck;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Direction;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.chubao.joyqueue.nsr.NameService;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
public class GetTopicsHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {
    private NameService nameService;
    @Override
    public int type() {
        return JoyQueueCommandType.MQTT_GET_TOPICS.getCode();
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.nameService = brokerContext.getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        GetTopics getTopics = (GetTopics) command.getPayload();
        Set<String> topics = new HashSet<>();
        if(StringUtils.isBlank(getTopics.getApp())){
            topics.addAll(nameService.getAllTopicCodes());
        }else {
            topics.addAll(nameService.getTopics(getTopics.getApp(), Subscription.Type.valueOf((byte)getTopics.getSubscribeType())));
        }
        return new Command(new JoyQueueHeader(Direction.RESPONSE, JoyQueueCommandType.MQTT_GET_TOPICS_ACK.getCode()),new GetTopicsAck().topics(topics));
    }
}
