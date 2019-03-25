package com.jd.journalq.broker.index.handler;

import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.index.command.ConsumeIndexStoreRequest;
import com.jd.journalq.broker.index.command.ConsumeIndexStoreResponse;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.command.handler.CommandHandler;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.broker.index.model.IndexAndMetadata;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexStoreHandler implements CommandHandler, Type {
    private BrokerContext brokerContext;
    private Consume consume;

    public ConsumeIndexStoreHandler(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.consume = brokerContext.getConsume();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        ConsumeIndexStoreRequest request = (ConsumeIndexStoreRequest) command.getPayload();
        if (request == null) return null;

        // offset meta data to store
        // group by topic -> partition -> offset metadata
        Map<String, Map<Integer, IndexAndMetadata>> indexMetadata = request.getIndexMetadata();
        // offset meta data store status
        // group by topic -> partition -> return code
        Map<String, Map<Integer, Short>> indexStoreStatus = new HashedMap();

        String app = request.getApp();
        for (String topic : indexMetadata.keySet()) {
            // offset meta data of partition
            // partition -> offset meta data
            Map<Integer, IndexAndMetadata> partitionIndexes = indexMetadata.get(topic);
            Map<Integer, Short> partitionIndexStoreStatus = new HashedMap();

            for (int partition : partitionIndexes.keySet()) {
                // set consume index
                setConsumeIndex(topic, (short)partition, app, partitionIndexes.get(partition).getIndex());
                partitionIndexStoreStatus.put(partition, (short) JMQCode.SUCCESS.getCode());
            }
            indexStoreStatus.put(topic, partitionIndexStoreStatus);
        }

        JMQHeader header = new JMQHeader(Direction.RESPONSE, QosLevel.ONE_WAY, CommandType.CONSUME_INDEX_STORE_RESPONSE);
        ConsumeIndexStoreResponse offsetStoreResponse = new ConsumeIndexStoreResponse(indexStoreStatus);
        return new Command(header, offsetStoreResponse);
    }

    private void setConsumeIndex(String topic, short partition, String app, long offset) {
        Consumer consumer = new Consumer(topic, app);
        consume.setAckIndex(consumer, partition, offset);
        consume.setStartAckIndex(consumer, partition, -1);
    }


    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_REQUEST;
    }
}
