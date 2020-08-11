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
package org.joyqueue.broker.protocol.handler;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.command.RemoveConsumerRequest;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RemoveConsumerRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class RemoveConsumerRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(RemoveConsumerRequestHandler.class);

    private SessionManager sessionManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        RemoveConsumerRequest removeConsumerRequest = (RemoveConsumerRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(removeConsumerRequest.getApp())) {
            logger.warn("connection does not exist, transport: {}, app: {}", transport, removeConsumerRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        for (String topic : removeConsumerRequest.getTopics()) {
            String consumerId = connection.getConsumer(topic, removeConsumerRequest.getApp());
            if (StringUtils.isBlank(consumerId)) {
                continue;
            }
            sessionManager.removeConsumer(consumerId);
        }

        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JoyQueueCommandType.REMOVE_CONSUMER_REQUEST.getCode();
    }
}
