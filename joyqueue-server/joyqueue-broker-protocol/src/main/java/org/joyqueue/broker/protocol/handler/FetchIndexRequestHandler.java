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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.FetchIndexData;
import org.joyqueue.network.command.FetchIndexRequest;
import org.joyqueue.network.command.FetchIndexResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * FetchIndexRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/13
 */
public class FetchIndexRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchIndexRequestHandler.class);

    private Consume consume;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.consume = brokerContext.getConsume();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchIndexRequest fetchIndexRequest = (FetchIndexRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchIndexRequest.getApp())) {
            logger.warn("connection does not exist, transport: {}, app: {}", transport, fetchIndexRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, FetchIndexData> result = HashBasedTable.create();

        for (Map.Entry<String, List<Short>> entry : fetchIndexRequest.getPartitions().entrySet()) {
            String topic = entry.getKey();
            Consumer consumer = new Consumer(connection.getId(), topic, fetchIndexRequest.getApp(), Consumer.ConsumeType.JOYQUEUE);
            for (Short partition : entry.getValue()) {
                FetchIndexData fetchIndexData = fetchIndex(connection, consumer, partition);
                result.put(topic, partition, fetchIndexData);
            }
        }

        FetchIndexResponse fetchIndexResponse = new FetchIndexResponse();
        fetchIndexResponse.setData(result);
        return new Command(fetchIndexResponse);
    }

    protected FetchIndexData fetchIndex(Connection connection, Consumer consumer, short partition) {
        FetchIndexData fetchIndexData = new FetchIndexData();
        try  {
            long leftIndex = consume.getMinIndex(consumer, partition);
            long rightIndex = consume.getMaxIndex(consumer, partition);
            long index = consume.getAckIndex(consumer, partition);
            fetchIndexData.setIndex(index);
            fetchIndexData.setLeftIndex(leftIndex);
            fetchIndexData.setRightIndex(rightIndex);
            fetchIndexData.setCode(JoyQueueCode.SUCCESS);
        } catch (Exception e) {
            fetchIndexData.setCode(JoyQueueCode.CN_UNKNOWN_ERROR);
            logger.error("fetchIndex exception, consumer: {}, partition: {}, transport: {}", consumer, partition, connection.getTransport(), e);
        }
        return fetchIndexData;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_INDEX_REQUEST.getCode();
    }
}
