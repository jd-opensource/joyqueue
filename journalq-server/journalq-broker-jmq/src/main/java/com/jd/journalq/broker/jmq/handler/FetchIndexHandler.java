package com.jd.journalq.broker.jmq.handler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.FetchIndex;
import com.jd.journalq.network.command.FetchIndexAck;
import com.jd.journalq.network.command.FetchIndexAckData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * FetchIndexHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndexHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchIndexHandler.class);

    private Consume consume;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.consume = brokerContext.getConsume();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchIndex fetchIndex = (FetchIndex) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchIndex.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, FetchIndexAckData> result = HashBasedTable.create();

        for (Map.Entry<String, List<Short>> entry : fetchIndex.getPartitions().entrySet()) {
            String topic = entry.getKey();
            Consumer consumer = new Consumer(connection.getId(), topic, fetchIndex.getApp(), Consumer.ConsumeType.JMQ);
            for (Short partition : entry.getValue()) {
                FetchIndexAckData fetchIndexAckData = fetchIndex(connection, consumer, partition);
                result.put(topic, partition, fetchIndexAckData);
            }
        }

        FetchIndexAck fetchIndexAck = new FetchIndexAck();
        fetchIndexAck.setData(result);
        return new Command(fetchIndexAck);
    }

    protected FetchIndexAckData fetchIndex(Connection connection, Consumer consumer, short partition) {
        FetchIndexAckData fetchIndexAckData = new FetchIndexAckData();
        try  {
            long index = consume.getAckIndex(consumer, partition);
            fetchIndexAckData.setIndex(index);
            fetchIndexAckData.setCode(JMQCode.SUCCESS);
        } catch (Exception e) {
            fetchIndexAckData.setCode(JMQCode.CN_UNKNOWN_ERROR);
            logger.error("fetchIndex exception, consumer: {}, partition: {}, transport: {}", consumer, partition, connection.getTransport(), e);
        }
        return fetchIndexAckData;
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_INDEX.getCode();
    }
}