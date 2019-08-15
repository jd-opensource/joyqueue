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
package io.chubao.joyqueue.broker.index.handler;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.index.command.ConsumeIndexQueryRequest;
import io.chubao.joyqueue.broker.index.command.ConsumeIndexQueryResponse;
import io.chubao.joyqueue.broker.index.model.IndexMetadataAndError;
import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.session.Consumer;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Direction;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import org.apache.commons.collections.map.HashedMap;

import java.util.List;
import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexQueryHandler implements CommandHandler, Type {
    private BrokerContext brokerContext;
    private Consume consume;

    public ConsumeIndexQueryHandler(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.consume = brokerContext.getConsume();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        ConsumeIndexQueryRequest request = (ConsumeIndexQueryRequest)command.getPayload();
        if (request == null) return null;

        Map<String, List<Integer>> indexTopicPartitions = request.getTopicPartitions();
        Map<String, Map<Integer, IndexMetadataAndError>> topicPartitionIndex = new HashedMap();
        String app = request.getApp();
        for (String topic : indexTopicPartitions.keySet()) {
            List<Integer> partitions = indexTopicPartitions.get(topic);
            Map<Integer, IndexMetadataAndError> partitionIndexes = new HashedMap();
            for (int partition : partitions) {
                long index = getConsumerIndex(topic, (short)partition, app);
                IndexMetadataAndError indexMetadataAndError = new IndexMetadataAndError(index, "", (short) JoyQueueCode.SUCCESS.getCode());
                partitionIndexes.put(partition, indexMetadataAndError);
            }
            topicPartitionIndex.put(topic, partitionIndexes);
        }
        ConsumeIndexQueryResponse response = new ConsumeIndexQueryResponse(topicPartitionIndex);
        JoyQueueHeader header = new JoyQueueHeader(Direction.RESPONSE, QosLevel.ONE_WAY, CommandType.CONSUME_INDEX_QUERY_RESPONSE);
        return new Command(header, response);
    }

    private long getConsumerIndex(String topic, short partition, String app) {
        Consumer consumer = new Consumer(topic, app);
        long ackCurIndex = consume.getAckIndex(consumer, partition);
        long ackStartIndex = consume.getStartIndex(consumer, partition);

        return ackCurIndex == ackStartIndex ? -1 : ackCurIndex;
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_REQUEST;
    }
}
