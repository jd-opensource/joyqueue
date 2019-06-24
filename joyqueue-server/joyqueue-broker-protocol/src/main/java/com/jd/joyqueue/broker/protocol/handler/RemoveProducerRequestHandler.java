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
package com.jd.joyqueue.broker.protocol.handler;

import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.BrokerContextAware;
import com.jd.joyqueue.broker.protocol.JournalqCommandHandler;
import com.jd.joyqueue.broker.helper.SessionHelper;
import com.jd.joyqueue.broker.monitor.SessionManager;
import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.network.command.BooleanAck;
import com.jd.joyqueue.network.command.JournalqCommandType;
import com.jd.joyqueue.network.command.RemoveProducerRequest;
import com.jd.joyqueue.network.session.Connection;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Type;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RemoveProducerRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class RemoveProducerRequestHandler implements JournalqCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(RemoveProducerRequestHandler.class);

    private SessionManager sessionManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        RemoveProducerRequest removeProducerRequest = (RemoveProducerRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(removeProducerRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, removeProducerRequest.getApp());
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        for (String topic : removeProducerRequest.getTopics()) {
            String producerId = connection.getProducer(topic, removeProducerRequest.getApp());
            if (StringUtils.isBlank(producerId)) {
                continue;
            }
            sessionManager.removeProducer(producerId);
        }

        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JournalqCommandType.REMOVE_PRODUCER_REQUEST.getCode();
    }
}