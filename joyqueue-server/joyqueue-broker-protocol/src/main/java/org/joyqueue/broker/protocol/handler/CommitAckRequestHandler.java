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
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.ConsumeConfig;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.domain.Partition;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.MessageLocation;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.CommitAckData;
import org.joyqueue.network.command.CommitAckRequest;
import org.joyqueue.network.command.CommitAckResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.command.RetryType;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.response.BooleanResponse;
import org.joyqueue.server.retry.api.MessageRetry;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.toolkit.lang.ListUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * CommitAckRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/12
 */
public class CommitAckRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(CommitAckRequestHandler.class);

    private Consume consume;
    private MessageRetry retryManager;
    private ClusterManager clusterManager;
    private ConsumeConfig consumeConfig;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.consume = brokerContext.getConsume();
        this.retryManager = brokerContext.getRetryManager();
        this.clusterManager = brokerContext.getClusterManager();
        this.consumeConfig = new ConsumeConfig(brokerContext.getPropertySupplier());
    }

    @Override
    public Command handle(Transport transport, Command command) {
        CommitAckRequest commitAckRequest = (CommitAckRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(commitAckRequest.getApp())) {
            logger.warn("connection does not exist, transport: {}, app: {}", transport, commitAckRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Table<String, Short, JoyQueueCode> result = HashBasedTable.create();

        for (Map.Entry<String, Map<Short, List<CommitAckData>>> entry : commitAckRequest.getData().rowMap().entrySet()) {
            String topic = entry.getKey();
            for (Map.Entry<Short, List<CommitAckData>> partitionEntry : entry.getValue().entrySet()) {
                JoyQueueCode ackCode = commitAck(connection, topic, commitAckRequest.getApp(), partitionEntry.getKey(), partitionEntry.getValue());
                result.put(topic, partitionEntry.getKey(), ackCode);
            }
        }

        CommitAckResponse commitAckResponse = new CommitAckResponse();
        commitAckResponse.setResult(result);
        return new Command(commitAckResponse);
    }

    protected JoyQueueCode commitAck(Connection connection, String topic, String app, short partition, List<CommitAckData> dataList) {
        if (partition == Partition.RETRY_PARTITION_ID) {
            return doCommitRetry(connection, topic, app, partition, dataList);
        } else {
            return doCommitAck(connection, topic, app, partition, dataList);
        }
    }

    protected JoyQueueCode doCommitRetry(Connection connection, String topic, String app, short partition, List<CommitAckData> dataList) {
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

            return JoyQueueCode.SUCCESS;
        } catch (JoyQueueException e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JoyQueueCode.valueOf(e.getCode());
        } catch (Exception e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JoyQueueCode.CN_UNKNOWN_ERROR;
        } finally {
            consume.releasePartition(topic, app, partition);
        }
    }

    protected JoyQueueCode doCommitAck(Connection connection, String topic, String app, short partition, List<CommitAckData> dataList) {
        BooleanResponse checkResponse = clusterManager.checkReadable(TopicName.parse(topic), app, connection.getHost(), partition);
        if (!checkResponse.isSuccess()) {
            logger.warn("check commit ack error, topic: {}, app: {}, partition: {}, transport: {}, code: {}", topic, app, partition, connection, checkResponse.getJoyQueueCode());
            return checkResponse.getJoyQueueCode();
        }

        MessageLocation[] messageLocations = new MessageLocation[dataList.size()];
        List<CommitAckData> retryDataList = null;
        Consumer consumer = new Consumer(connection.getId(), topic, app, Consumer.ConsumeType.JOYQUEUE);

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

        try {
            if (CollectionUtils.isNotEmpty(retryDataList)) {
                org.joyqueue.domain.Consumer subscribeConsumer = clusterManager.getNameService().getConsumerByTopicAndApp(TopicName.parse(consumer.getTopic()), consumer.getApp());
                if (subscribeConsumer != null && subscribeConsumer.getConsumerPolicy() != null
                        && subscribeConsumer.getConsumerPolicy().getRetry() != null && !subscribeConsumer.getConsumerPolicy().getRetry()) {

                    if (consumeConfig.getRetryForceAck(consumer.getTopic(), consumer.getApp())) {
                        consume.acknowledge(messageLocations, consumer, connection, true);
                        return JoyQueueCode.SUCCESS;
                    }

                    logger.warn("consumer retry is disabled, ignore retry, topic: {}, app: {}", consumer.getTopic(), consumer.getApp());
                    consume.releasePartition(topic, app, partition);
                    return JoyQueueCode.SUCCESS;
                }

                try {
                    commitRetry(connection, consumer, retryDataList);
                } catch (JoyQueueException e) {
                    logger.error("commit retry exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
                    consume.releasePartition(topic, app, partition);
                    return JoyQueueCode.valueOf(e.getCode());
                } catch (Exception e) {
                    logger.error("commit retry exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
                    consume.releasePartition(topic, app, partition);
                    return JoyQueueCode.CN_UNKNOWN_ERROR;
                }
            }

            consume.acknowledge(messageLocations, consumer, connection, true);
            return JoyQueueCode.SUCCESS;
        } catch (JoyQueueException e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JoyQueueCode.valueOf(e.getCode());
        } catch (Exception e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JoyQueueCode.CN_UNKNOWN_ERROR;
        }
    }

    protected void commitRetry(Connection connection, Consumer consumer, List<CommitAckData> data) throws Exception {
        List<RetryMessageModel> retryMessageModelList = Lists.newLinkedList();
        for (CommitAckData ackData : data) {
            PullResult pullResult = consume.getMessage(consumer, ackData.getPartition(), ackData.getIndex(), 1);
            List<ByteBuffer> buffers = pullResult.getBuffers();

            if (buffers.size() != 1) {
                logger.error("get retryMessage error, message not exist, transport: {}, topic: {}, app: {}, partition: {}, index: {}",
                        connection.getTransport().remoteAddress(), consumer.getTopic(), consumer.getApp(), ackData.getPartition(), ackData.getIndex());
                continue;
            }

            ByteBuffer buffer = buffers.get(0);
            BrokerMessage brokerMessage = Serializer.readBrokerMessage(buffer);
            RetryMessageModel model = generateRetryMessage(consumer, brokerMessage, buffer.array(), ackData.getRetryType().name());
            retryMessageModelList.add(model);
        }
        retryManager.addRetry(retryMessageModelList);
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
        return JoyQueueCommandType.COMMIT_ACK_REQUEST.getCode();
    }
}
