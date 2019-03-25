package com.jd.journalq.broker.index.handler;

import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.domain.QosLevel;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.command.handler.CommandHandler;
import com.jd.journalq.common.network.session.Consumer;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Direction;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.common.network.transport.exception.TransportException;
import com.jd.journalq.broker.index.command.ConsumeIndexQueryRequest;
import com.jd.journalq.broker.index.command.ConsumeIndexQueryResponse;
import com.jd.journalq.broker.index.model.IndexMetadataAndError;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;
import java.util.Set;

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

        Map<String, Set<Integer>> indexTopicPartitions = request.getTopicPartitions();
        Map<String, Map<Integer, IndexMetadataAndError>> topicPartitionIndex = new HashedMap();
        String app = request.getApp();
        for (String topic : indexTopicPartitions.keySet()) {
            Set<Integer> partitions = indexTopicPartitions.get(topic);
            Map<Integer, IndexMetadataAndError> partitionIndexes = new HashedMap();
            for (int partition : partitions) {
                long index = getConsumerIndex(topic, (short)partition, app);
                IndexMetadataAndError indexMetadataAndError = new IndexMetadataAndError(index, "", (short)JMQCode.SUCCESS.getCode());
                partitionIndexes.put(partition, indexMetadataAndError);
            }
            topicPartitionIndex.put(topic, partitionIndexes);
        }
        ConsumeIndexQueryResponse response = new ConsumeIndexQueryResponse(topicPartitionIndex);
        JMQHeader header = new JMQHeader(Direction.RESPONSE, QosLevel.ONE_WAY, CommandType.CONSUME_INDEX_QUERY_RESPONSE);
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
