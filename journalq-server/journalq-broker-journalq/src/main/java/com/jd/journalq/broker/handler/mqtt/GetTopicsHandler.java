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
import com.jd.journalq.domain.Subscription;
import com.jd.journalq.network.command.GetTopics;
import com.jd.journalq.network.command.GetTopicsAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.nsr.NameService;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
public class GetTopicsHandler implements JournalqCommandHandler, Type, BrokerContextAware {
    private NameService nameService;
    @Override
    public int type() {
        return JournalqCommandType.MQTT_GET_TOPICS.getCode();
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
            topics.addAll(nameService.getAllTopics());
        }else {
            topics.addAll(nameService.getTopics(getTopics.getApp(), Subscription.Type.valueOf((byte)getTopics.getSubscribeType())));
        }
        return new Command(new JMQHeader(Direction.RESPONSE, JournalqCommandType.MQTT_GET_TOPICS_ACK.getCode()),new GetTopicsAck().topics(topics));
    }
}
