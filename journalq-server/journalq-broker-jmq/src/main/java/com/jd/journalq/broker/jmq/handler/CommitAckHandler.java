package com.jd.journalq.broker.jmq.handler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.common.domain.Partition;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.common.message.BrokerMessage;
import com.jd.journalq.common.message.MessageLocation;
import com.jd.journalq.common.network.command.BooleanAck;
import com.jd.journalq.common.network.command.CommitAck;
import com.jd.journalq.common.network.command.CommitAckAck;
import com.jd.journalq.common.network.command.CommitAckData;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.command.RetryType;
import com.jd.journalq.common.network.session.Connection;
import com.jd.journalq.common.network.session.Consumer;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.server.retry.api.MessageRetry;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.toolkit.lang.ListUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * CommitAckHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(CommitAckHandler.class);

    private Consume consume;
    private MessageRetry retryManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.consume = brokerContext.getConsume();
        this.retryManager = brokerContext.getRetryManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        CommitAck commitAck = (CommitAck) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(commitAck.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, JMQCode> result = HashBasedTable.create();

        for (Map.Entry<String, Map<Short, List<CommitAckData>>> entry : commitAck.getData().rowMap().entrySet()) {
            String topic = entry.getKey();
            for (Map.Entry<Short, List<CommitAckData>> partitionEntry : entry.getValue().entrySet()) {
                JMQCode ackCode = commitAck(connection, topic, commitAck.getApp(), partitionEntry.getKey(), partitionEntry.getValue());
                result.put(topic, partitionEntry.getKey(), ackCode);
            }
        }

        CommitAckAck commitAckAck = new CommitAckAck();
        commitAckAck.setResult(result);
        return new Command(commitAckAck);
    }

    protected JMQCode commitAck(Connection connection, String topic, String app, short partition, List<CommitAckData> dataList) {
        if (partition == Partition.RETRY_PARTITION_ID) {
            return doCommitRetry(connection, topic, app, partition, dataList);
        } else {
            return doCommitAck(connection, topic, app, partition, dataList);
        }
    }

    protected JMQCode doCommitRetry(Connection connection, String topic, String app, short partition, List<CommitAckData> dataList) {
        try {
            List<Long> retrySuccess = Lists.newLinkedList();
            List<Long> retryError = Lists.newLinkedList();

            for (CommitAckData commitAckData : dataList) {
                if (commitAckData.getRetryType().equals(RetryType.NONE)) {
                    retrySuccess.add(commitAckData.getIndex());
                } else {
                    retryError.add(commitAckData.getIndex());
                }
            }

            if (CollectionUtils.isNotEmpty(retrySuccess)) {
                retryManager.retrySuccess(topic, app, ListUtil.toArray(retrySuccess));
            }

            if (CollectionUtils.isNotEmpty(retryError)) {
                retryManager.retryError(topic, app, ListUtil.toArray(retryError));
            }

            return JMQCode.SUCCESS;
        } catch (JMQException e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JMQCode.valueOf(e.getCode());
        } catch (Exception e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JMQCode.CN_UNKNOWN_ERROR;
        }
    }

    protected JMQCode doCommitAck(Connection connection, String topic, String app, short partition, List<CommitAckData> dataList) {
        try {
            MessageLocation[] messageLocations = new MessageLocation[dataList.size()];
            List<CommitAckData> retryDataList = null;
            Consumer consumer = new Consumer(connection.getId(), topic, app, Consumer.ConsumeType.JMQ);

            for (int i = 0; i < dataList.size(); i++) {
                CommitAckData data = dataList.get(i);
                messageLocations[i] = new MessageLocation(topic, partition, data.getIndex());

                if (!data.getRetryType().equals(RetryType.NONE)) {
                    if (retryDataList == null) {
                        retryDataList = Lists.newLinkedList();
                    }
                    retryDataList.add(data);
                }
            }

            consume.acknowledge(messageLocations, consumer, connection, true);

            if (CollectionUtils.isNotEmpty(retryDataList)) {
                commitRetry(connection, consumer, retryDataList);
            }

            return JMQCode.SUCCESS;
        } catch (JMQException e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JMQCode.valueOf(e.getCode());
        } catch (Exception e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JMQCode.CN_UNKNOWN_ERROR;
        }
    }

    protected void commitRetry(Connection connection, Consumer consumer, List<CommitAckData> data) throws JMQException {
        for (CommitAckData ackData : data) {
            PullResult pullResult = consume.getMessage(consumer, ackData.getPartition(), ackData.getIndex(), 1);
            List<ByteBuffer> buffers = pullResult.getBuffers();

            if (buffers.size() != 1) {
                logger.error("get retryMessage error, message not exist, transport: {}, topic: {}, partition: {}, index: {}",
                        connection.getTransport().remoteAddress(), consumer.getTopic(), ackData.getPartition(), ackData.getIndex());
                continue;
            }

            try {
                ByteBuffer buffer = buffers.get(0);
                BrokerMessage brokerMessage = Serializer.readBrokerMessage(buffer);
                RetryMessageModel model = generateRetryMessage(consumer, brokerMessage, buffer.array(), ackData.getRetryType().name());
                retryManager.addRetry(Lists.newArrayList(model));
            } catch (Exception e) {
                logger.error("add retryMessage exception, transport: {}, topic: {}, partition: {}, index: {}",
                        connection.getTransport().remoteAddress(), consumer.getTopic(), ackData.getPartition(), ackData.getIndex(), e);
            }
        }
    }

    private RetryMessageModel generateRetryMessage(Consumer consumer, BrokerMessage brokerMessage, byte[] brokerMessageData/* BrokerMessage 序列化后的字节数组 */, String exception) {
        RetryMessageModel model = new RetryMessageModel();
        model.setBusinessId(brokerMessage.getBusinessId());
        model.setTopic(consumer.getTopic());
        model.setApp(consumer.getApp());
        model.setPartition(Partition.RETRY_PARTITION_ID);
        model.setIndex(brokerMessage.getMsgIndexNo());
        model.setBrokerMessage(brokerMessageData);
        byte[] exceptionBytes = exception.getBytes(Charset.forName("UTF-8"));
        model.setException(exceptionBytes);
        model.setSendTime(brokerMessage.getStartTime());

        return model;
    }


    @Override
    public int type() {
        return JMQCommandType.COMMIT_ACK.getCode();
    }
}