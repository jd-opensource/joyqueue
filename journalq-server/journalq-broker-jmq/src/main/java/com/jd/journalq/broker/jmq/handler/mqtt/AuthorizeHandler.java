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
package com.jd.journalq.broker.jmq.handler.mqtt;

import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.Authorization;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
public class AuthorizeHandler implements JMQCommandHandler, Type, BrokerContextAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ClusterManager clusterManager;
    @Override
    public int type() {
        return JMQCommandType.MQTT_AUTHORIZATION.getCode();
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        Authorization authorization = (Authorization) command.getPayload();
        return clusterManager.doAuthorization(authorization.getApp(),authorization.getToken()) ? BooleanAck.build():BooleanAck.build(JMQCode.CN_AUTHENTICATION_ERROR);
    }
}
