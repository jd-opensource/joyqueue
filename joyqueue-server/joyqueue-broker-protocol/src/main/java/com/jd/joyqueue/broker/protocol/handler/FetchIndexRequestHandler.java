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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.BrokerContextAware;
import com.jd.joyqueue.broker.protocol.JournalqCommandHandler;
import com.jd.joyqueue.broker.consumer.Consume;
import com.jd.joyqueue.broker.helper.SessionHelper;
import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.network.command.BooleanAck;
import com.jd.joyqueue.network.command.FetchIndexAckData;
import com.jd.joyqueue.network.command.FetchIndexRequest;
import com.jd.joyqueue.network.command.FetchIndexResponse;
import com.jd.joyqueue.network.command.JournalqCommandType;
import com.jd.joyqueue.network.session.Connection;
import com.jd.joyqueue.network.session.Consumer;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * FetchIndexRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndexRequestHandler implements JournalqCommandHandler, Type, BrokerContextAware {

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
            logger.warn("connection is not exists, transport: {}, app: {}", transport, fetchIndexRequest.getApp());
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, FetchIndexAckData> result = HashBasedTable.create();

        for (Map.Entry<String, List<Short>> entry : fetchIndexRequest.getPartitions().entrySet()) {
            String topic = entry.getKey();
            Consumer consumer = new Consumer(connection.getId(), topic, fetchIndexRequest.getApp(), Consumer.ConsumeType.JMQ);
            for (Short partition : entry.getValue()) {
                FetchIndexAckData fetchIndexAckData = fetchIndex(connection, consumer, partition);
                result.put(topic, partition, fetchIndexAckData);
            }
        }

        FetchIndexResponse fetchIndexResponse = new FetchIndexResponse();
        fetchIndexResponse.setData(result);
        return new Command(fetchIndexResponse);
    }

    protected FetchIndexAckData fetchIndex(Connection connection, Consumer consumer, short partition) {
        FetchIndexAckData fetchIndexAckData = new FetchIndexAckData();
        try  {
            long index = consume.getAckIndex(consumer, partition);
            fetchIndexAckData.setIndex(index);
            fetchIndexAckData.setCode(JournalqCode.SUCCESS);
        } catch (Exception e) {
            fetchIndexAckData.setCode(JournalqCode.CN_UNKNOWN_ERROR);
            logger.error("fetchIndex exception, consumer: {}, partition: {}, transport: {}", consumer, partition, connection.getTransport(), e);
        }
        return fetchIndexAckData;
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_INDEX_REQUEST.getCode();
    }
}