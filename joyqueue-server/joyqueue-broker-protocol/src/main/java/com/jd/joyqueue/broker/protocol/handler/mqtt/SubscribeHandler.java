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
package com.jd.joyqueue.broker.protocol.handler.mqtt;

import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.BrokerContextAware;
import com.jd.joyqueue.broker.protocol.JoyQueueCommandHandler;
import com.jd.joyqueue.domain.TopicConfig;
import com.jd.joyqueue.network.command.JoyQueueCommandType;
import com.jd.joyqueue.network.command.Subscribe;
import com.jd.joyqueue.network.command.SubscribeAck;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.codec.JoyQueueHeader;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Direction;
import com.jd.joyqueue.network.transport.command.Type;
import com.jd.joyqueue.nsr.NameService;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
@Deprecated
public class SubscribeHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {
    private NameService nameService;

    @Override
    public int type() {
        return JoyQueueCommandType.MQTT_SUBSCRIBE.getCode();
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.nameService = brokerContext.getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        Subscribe subscribe = (Subscribe) command.getPayload();
        List<TopicConfig> topicConfigs = nameService.subscribe(subscribe.getSubscriptions(), subscribe.getClientType());
        return new Command(new JoyQueueHeader(Direction.RESPONSE, JoyQueueCommandType.MQTT_SUBSCRIBE_ACK.getCode()), new SubscribeAck().topicConfigs(topicConfigs));
    }
}
