package io.chubao.joyqueue.broker.protocol.handler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.FetchIndexAckData;
import io.chubao.joyqueue.network.command.FetchIndexRequest;
import io.chubao.joyqueue.network.command.FetchIndexResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.session.Consumer;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
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
            logger.warn("connection is not exists, transport: {}, app: {}", transport, fetchIndexRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, FetchIndexAckData> result = HashBasedTable.create();

        for (Map.Entry<String, List<Short>> entry : fetchIndexRequest.getPartitions().entrySet()) {
            String topic = entry.getKey();
            Consumer consumer = new Consumer(connection.getId(), topic, fetchIndexRequest.getApp(), Consumer.ConsumeType.JOYQUEUE);
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
            fetchIndexAckData.setCode(JoyQueueCode.SUCCESS);
        } catch (Exception e) {
            fetchIndexAckData.setCode(JoyQueueCode.CN_UNKNOWN_ERROR);
            logger.error("fetchIndex exception, consumer: {}, partition: {}, transport: {}", consumer, partition, connection.getTransport(), e);
        }
        return fetchIndexAckData;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_INDEX_REQUEST.getCode();
    }
}