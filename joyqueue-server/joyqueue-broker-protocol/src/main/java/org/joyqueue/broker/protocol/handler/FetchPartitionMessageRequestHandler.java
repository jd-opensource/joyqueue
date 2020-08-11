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
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.broker.protocol.command.FetchPartitionMessageRequest;
import org.joyqueue.broker.protocol.command.FetchPartitionMessageResponse;
import org.joyqueue.broker.protocol.converter.CheckResultConverter;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.FetchPartitionMessageAckData;
import org.joyqueue.network.command.FetchPartitionMessageData;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.protocol.annotation.FetchHandler;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * FetchPartitionMessageRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/13
 */
@FetchHandler
public class FetchPartitionMessageRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

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
            logger.warn("connection does not exist, transport: {}, app: {}", transport, fetchPartitionMessageRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, FetchPartitionMessageAckData> result = HashBasedTable.create();
        Traffic traffic = new Traffic(fetchPartitionMessageRequest.getApp());

        for (Map.Entry<String, Map<Short, FetchPartitionMessageData>> entry : fetchPartitionMessageRequest.getPartitions().rowMap().entrySet()) {
            String topic = entry.getKey();
            Consumer consumer = new Consumer(connection.getId(), topic, fetchPartitionMessageRequest.getApp(), Consumer.ConsumeType.JOYQUEUE);
            for (Map.Entry<Short, FetchPartitionMessageData> partitionEntry : entry.getValue().entrySet()) {
                short partition = partitionEntry.getKey();

                BooleanResponse checkResult = clusterManager.checkReadable(TopicName.parse(topic), fetchPartitionMessageRequest.getApp(),
                        connection.getHost(), partition);
                if (!checkResult.isSuccess()) {
                    logger.warn("checkReadable failed, transport: {}, topic: {}, partition: {}, app: {}, code: {}", transport,
                            consumer.getTopic(), partition, consumer.getApp(), checkResult.getJoyQueueCode());
                    result.put(topic, partitionEntry.getKey(),
                            new FetchPartitionMessageAckData(CheckResultConverter.convertFetchCode(command.getHeader().getVersion(), checkResult.getJoyQueueCode())));
                    continue;
                }

                if (fetchPartitionMessageRequest.getTraffic().isLimited(entry.getKey())) {
                    result.put(topic, partitionEntry.getKey(), new FetchPartitionMessageAckData(JoyQueueCode.SUCCESS));
                    continue;
                }

                FetchPartitionMessageData fetchPartitionMessageData = partitionEntry.getValue();
                FetchPartitionMessageAckData fetchPartitionMessageAckData = fetchMessage(transport, consumer, partition,
                        fetchPartitionMessageData.getIndex(), fetchPartitionMessageData.getCount());
                result.put(topic, partitionEntry.getKey(), fetchPartitionMessageAckData);
                traffic.record(topic, fetchPartitionMessageAckData.getTraffic(), fetchPartitionMessageAckData.getSize());
            }
        }

        FetchPartitionMessageResponse fetchPartitionMessageResponse = new FetchPartitionMessageResponse();
        fetchPartitionMessageResponse.setTraffic(traffic);
        fetchPartitionMessageResponse.setData(result);
        return new Command(fetchPartitionMessageResponse);
    }

    protected FetchPartitionMessageAckData fetchMessage(Transport transport, Consumer consumer, short partition, long index, int count) {
        FetchPartitionMessageAckData fetchPartitionMessageAckData = new FetchPartitionMessageAckData();
        fetchPartitionMessageAckData.setBuffers(Collections.emptyList());
        try {
            long minIndex = consume.getMinIndex(consumer, partition);
            long maxIndex = consume.getMaxIndex(consumer, partition);
            if (index == FetchPartitionMessageRequest.NONE_INDEX) {
                index = consume.getAckIndex(consumer, partition);
                if (index < minIndex) {
                    logger.warn("fetchPartitionMessage exception, index reset to minIndex, transport: {}, consumer: {}, partition: {}, index: {}, minIndex: {}, maxIndex: {}",
                            transport, consumer, partition, index, minIndex, maxIndex);
                    index = minIndex;
                }
            }
            if (index < minIndex || index > maxIndex) {
                logger.warn("fetchPartitionMessage exception, index ou of range, transport: {}, consumer: {}, partition: {}, index: {}, minIndex: {}, maxIndex: {}",
                        transport, consumer, partition, index, minIndex, maxIndex);
                fetchPartitionMessageAckData.setCode(JoyQueueCode.FW_FETCH_MESSAGE_INDEX_OUT_OF_RANGE);
            } else {
                PullResult pullResult = consume.getMessage(consumer, partition, index, count);
                if (!pullResult.getCode().equals(JoyQueueCode.SUCCESS)) {
                    logger.error("fetchPartitionMessage exception, transport: {}, consumer: {}, partition: {}, index: {}, minIndex: {}, maxIndex: {}",
                            transport, consumer, partition, index, minIndex, maxIndex);
                }
                fetchPartitionMessageAckData.setBuffers(pullResult.getBuffers());
                fetchPartitionMessageAckData.setCode(pullResult.getCode());
            }
        } catch (JoyQueueException e) {
            logger.error("fetchPartitionMessage exception, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index, e);
            fetchPartitionMessageAckData.setCode(JoyQueueCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("fetchPartitionMessage exception, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index, e);
            fetchPartitionMessageAckData.setCode(JoyQueueCode.CN_UNKNOWN_ERROR);
        }
        return fetchPartitionMessageAckData;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_PARTITION_MESSAGE_REQUEST.getCode();
    }
}
