package com.jd.journalq.broker.jmq.handler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.converter.CheckResultConverter;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.FetchPartitionMessage;
import com.jd.journalq.network.command.FetchPartitionMessageAck;
import com.jd.journalq.network.command.FetchPartitionMessageAckData;
import com.jd.journalq.network.command.FetchPartitionMessageData;
import com.jd.journalq.network.command.JMQCommandType;
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
 * FetchPartitionMessageHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchPartitionMessageHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchPartitionMessageHandler.class);

    private Consume consume;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.consume = brokerContext.getConsume();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchPartitionMessage fetchPartitionMessage = (FetchPartitionMessage) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchPartitionMessage.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, FetchPartitionMessageAckData> result = HashBasedTable.create();

        for (Map.Entry<String, Map<Short, FetchPartitionMessageData>> entry : fetchPartitionMessage.getPartitions().rowMap().entrySet()) {
            String topic = entry.getKey();
            Consumer consumer = new Consumer(connection.getId(), topic, fetchPartitionMessage.getApp(), Consumer.ConsumeType.JMQ);
            for (Map.Entry<Short, FetchPartitionMessageData> partitionEntry : entry.getValue().entrySet()) {
                short partition = partitionEntry.getKey();

                BooleanResponse checkResult = clusterManager.checkReadable(TopicName.parse(topic), fetchPartitionMessage.getApp(), connection.getHost(), partition);
                if (!checkResult.isSuccess()) {
                    logger.warn("checkReadable failed, transport: {}, topic: {}, app: {}", transport, consumer.getTopic(), consumer.getApp());
                    buildFetchPartitionMessageAckData(topic, entry.getValue(), CheckResultConverter.convertFetchCode(checkResult.getJmqCode()), result);
                    continue;
                }

                FetchPartitionMessageData fetchPartitionMessageData = partitionEntry.getValue();
                FetchPartitionMessageAckData fetchPartitionMessageAckData = fetchMessage(transport, consumer, partition, fetchPartitionMessageData.getIndex(), fetchPartitionMessageData.getCount());
                result.put(topic, partitionEntry.getKey(), fetchPartitionMessageAckData);
            }
        }

        FetchPartitionMessageAck fetchPartitionMessageAck = new FetchPartitionMessageAck();
        fetchPartitionMessageAck.setData(result);
        return new Command(fetchPartitionMessageAck);
    }

    protected void buildFetchPartitionMessageAckData(String topic, Map<Short, FetchPartitionMessageData> partitionMap, JMQCode code, Table<String, Short, FetchPartitionMessageAckData> result) {
        FetchPartitionMessageAckData fetchPartitionMessageAckData = new FetchPartitionMessageAckData(code);
        for (Map.Entry<Short, FetchPartitionMessageData> entry : partitionMap.entrySet()) {
            result.put(topic, entry.getKey(), fetchPartitionMessageAckData);
        }
    }

    protected FetchPartitionMessageAckData fetchMessage(Transport transport, Consumer consumer, short partition, long index, int count) {
        FetchPartitionMessageAckData fetchPartitionMessageAckData = new FetchPartitionMessageAckData();
        fetchPartitionMessageAckData.setBuffers(Collections.emptyList());
        try {
            if (index == FetchPartitionMessage.NONE_INDEX) {
                index = consume.getAckIndex(consumer, partition);
            }
            if (index < consume.getMinIndex(consumer, partition) || index > consume.getMaxIndex(consumer, partition)) {
                logger.warn("fetchPartitionMessage exception, index ou of range, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index);
                fetchPartitionMessageAckData.setCode(JMQCode.FW_FETCH_MESSAGE_INDEX_OUT_OF_RANGE);
            } else {
                PullResult pullResult = consume.getMessage(consumer, partition, index, count);
                if (!pullResult.getJmqCode().equals(JMQCode.SUCCESS)) {
                    logger.error("fetchPartitionMessage exception, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index);
                }
                fetchPartitionMessageAckData.setBuffers(pullResult.getBuffers());
                fetchPartitionMessageAckData.setCode(pullResult.getJmqCode());
            }
        } catch (JMQException e) {
            logger.error("fetchPartitionMessage exception, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index, e);
            fetchPartitionMessageAckData.setCode(JMQCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("fetchPartitionMessage exception, transport: {}, consumer: {}, partition: {}, index: {}", transport, consumer, partition, index, e);
            fetchPartitionMessageAckData.setCode(JMQCode.CN_UNKNOWN_ERROR);
        }
        return fetchPartitionMessageAckData;
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_PARTITION_MESSAGE.getCode();
    }
}