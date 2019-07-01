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
package com.jd.journalq.broker.protocol.handler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.network.traffic.Traffic;
import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.broker.protocol.command.FetchPartitionMessageResponse;
import com.jd.journalq.broker.protocol.converter.CheckResultConverter;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.FetchPartitionMessageAckData;
import com.jd.journalq.network.command.FetchPartitionMessageData;
import com.jd.journalq.network.command.FetchPartitionMessageRequest;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * FetchPartitionMessageRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchPartitionMessageRequestHandler implements JournalqCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchPartitionMessageRequestHandler.class);

    private Consume consume;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.consume = brokerContext.getConsume();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchPartitionMessageRequest fetchPartitionMessageRequest = (FetchPartitionMessageRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchPartitionMessageRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, fetchPartitionMessageRequest.getApp());
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, FetchPartitionMessageAckData> result = HashBasedTable.create();
        Traffic traffic = new Traffic(fetchPartitionMessageRequest.getApp());

        for (Map.Entry<String, Map<Short, FetchPartitionMessageData>> entry : fetchPartitionMessageRequest.getPartitions().rowMap().entrySet()) {
            String topic = entry.getKey();
            Consumer consumer = new Consumer(connection.getId(), topic, fetchPartitionMessageRequest.getApp(), Consumer.ConsumeType.JMQ);
            for (Map.Entry<Short, FetchPartitionMessageData> partitionEntry : entry.getValue().entrySet()) {
                short partition = partitionEntry.getKey();

                BooleanResponse checkResult = clusterManager.checkReadable(TopicName.parse(topic), fetchPartitionMessageRequest.getApp(),
                        connection.getHost(), partition);
                if (!checkResult.isSuccess()) {
                    logger.warn("checkReadable failed, transport: {}, topic: {}, partition: {}, app: {}, code: {}", transport,
                            consumer.getTopic(), partition, consumer.getApp(), checkResult.getJournalqCode());
                    buildFetchPartitionMessageAckData(topic, entry.getValue(), CheckResultConverter.convertFetchCode(checkResult.getJournalqCode()), result);
                    traffic.record(topic, 0);
                    continue;
                }

                FetchPartitionMessageData fetchPartitionMessageData = partitionEntry.getValue();
                FetchPartitionMessageAckData fetchPartitionMessageAckData = fetchMessage(transport, consumer, partition,
                        fetchPartitionMessageData.getIndex(), fetchPartitionMessageData.getCount());
                result.put(topic, partitionEntry.getKey(), fetchPartitionMessageAckData);
                traffic.record(topic, fetchPartitionMessageAckData.getSize());
            }
        }

        FetchPartitionMessageResponse fetchPartitionMessageResponse = new FetchPartitionMessageResponse();
        fetchPartitionMessageResponse.setTraffic(traffic);
        fetchPartitionMessageResponse.setData(result);
        return new Command(fetchPartitionMessageResponse);
    }

    protected void buildFetchPartitionMessageAckData(String topic, Map<Short, FetchPartitionMessageData> partitionMap, JournalqCode code, Table<String, Short, FetchPartitionMessageAckData> result) {
        FetchPartitionMessageAckData fetchPartitionMessageAckData = new FetchPartitionMessageAckData(code);
        for (Map.Entry<Short, FetchPartitionMessageData> entry : partitionMap.entrySet()) {
            result.put(topic, entry.getKey(), fetchPartitionMessageAckData);
        }
    }

    protected FetchPartitionMessageAckData fetchMessage(Transport transport, Consumer consumer, short partition, long index, int count) {
        FetchPartitionMessageAckData fetchPartitionMessageAckData = new FetchPartitionMessageAckData();
        fetchPartitionMessageAckData.setBuffers(Collections.emptyList());
        try {
            if (index == FetchPartitionMessageRequest.NONE_INDEX) {
                index = consume.getAckIndex(consumer, partition);
            }
            if (index < consume.getMinIndex(consumer, partition) || index > consume.getMaxIndex(consumer, partition)) {
                logger.warn("fetchPartitionMessage exception, index ou of range, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index);
                fetchPartitionMessageAckData.setCode(JournalqCode.FW_FETCH_MESSAGE_INDEX_OUT_OF_RANGE);
            } else {
                PullResult pullResult = consume.getMessage(consumer, partition, index, count);
                if (!pullResult.getJournalqCode().equals(JournalqCode.SUCCESS)) {
                    logger.error("fetchPartitionMessage exception, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index);
                }
                fetchPartitionMessageAckData.setBuffers(pullResult.getBuffers());
                fetchPartitionMessageAckData.setCode(pullResult.getJournalqCode());
            }
        } catch (JournalqException e) {
            logger.error("fetchPartitionMessage exception, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index, e);
            fetchPartitionMessageAckData.setCode(JournalqCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("fetchPartitionMessage exception, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index, e);
            fetchPartitionMessageAckData.setCode(JournalqCode.CN_UNKNOWN_ERROR);
        }
        return fetchPartitionMessageAckData;
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_PARTITION_MESSAGE_REQUEST.getCode();
    }
}