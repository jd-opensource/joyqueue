package org.joyqueue.broker.protocol.handler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.CommitIndexRequest;
import org.joyqueue.network.command.CommitIndexResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * CommitIndexRequestHandler
 * author: gaohaoxiang
 * date: 2020/5/20
 */
public class CommitIndexRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(CommitIndexRequestHandler.class);

    private ClusterManager clusterManager;
    private Consume consume;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
        this.consume = brokerContext.getConsume();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        CommitIndexRequest commitIndexRequest = (CommitIndexRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);
        String app = commitIndexRequest.getApp();

        if (connection == null || !connection.isAuthorized(app)) {
            logger.warn("connection does not exist, transport: {}, app: {}", transport, app);
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, JoyQueueCode> result = HashBasedTable.create();

        for (Map.Entry<String, Map<Short, Long>> entry : commitIndexRequest.getData().rowMap().entrySet()) {
            String topic = entry.getKey();
            for (Map.Entry<Short, Long> partitionEntry : entry.getValue().entrySet()) {
                short partition = partitionEntry.getKey();
                long index = partitionEntry.getValue();

                BooleanResponse checkResponse = clusterManager.checkReadable(TopicName.parse(topic), app, connection.getHost(), partition);
                if (!checkResponse.isSuccess()) {
                    logger.warn("check commit index error, topic: {}, app: {}, partition: {}, transport: {}, code: {}", topic, app, partition, connection, checkResponse.getJoyQueueCode());
                    result.put(topic, partition, checkResponse.getJoyQueueCode());
                    continue;
                }

                try {
                    Consumer consumer = new Consumer(topic, app);

                    if (index == CommitIndexRequest.MAX_INDEX) {
                        index = consume.getMaxIndex(consumer, partition);
                    } else if (index == CommitIndexRequest.MIN_INDEX) {
                        index = consume.getMinIndex(consumer, partition);
                    }

                    consume.setAckIndex(consumer, partition, index);
                } catch (JoyQueueException e) {
                    logger.error("commit index exception, topic: {}, app: {}, partition: {}, transport: {}, code: {}", topic, app, partition, connection, JoyQueueCode.valueOf(e.getCode()));
                    result.put(topic, partition, JoyQueueCode.valueOf(e.getCode()));
                } catch (Exception e) {
                    logger.error("commit index exception, topic: {}, app: {}, partition: {}, transport: {}, code: {}", topic, app, partition, connection, e);
                    result.put(topic, partition, JoyQueueCode.CN_UNKNOWN_ERROR);
                }
            }
        }

        CommitIndexResponse commitIndexResponse = new CommitIndexResponse();
        commitIndexResponse.setResult(result);
        return new Command(commitIndexResponse);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.COMMIT_ACK_INDEX_REQUEST.getCode();
    }
}
