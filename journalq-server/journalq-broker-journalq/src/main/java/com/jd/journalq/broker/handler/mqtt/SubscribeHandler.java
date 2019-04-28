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
package com.jd.journalq.broker.handler.mqtt;

import com.jd.journalq.broker.JournalqCommandHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.command.Subscribe;
import com.jd.journalq.network.command.SubscribeAck;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.NameService;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
@Deprecated
public class SubscribeHandler implements JournalqCommandHandler, Type, BrokerContextAware {
    private NameService nameService;

    @Override
    public int type() {
        return JournalqCommandType.MQTT_SUBSCRIBE.getCode();
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.nameService = brokerContext.getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        Subscribe subscribe = (Subscribe) command.getPayload();
        List<TopicConfig> topicConfigs = nameService.subscribe(subscribe.getSubscriptions(), subscribe.getClientType());
        return new Command(new JMQHeader(Direction.RESPONSE, JournalqCommandType.MQTT_SUBSCRIBE_ACK.getCode()), new SubscribeAck().topicConfigs(topicConfigs));
    }
}
