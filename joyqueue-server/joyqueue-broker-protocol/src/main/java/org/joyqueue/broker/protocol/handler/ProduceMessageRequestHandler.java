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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.broker.producer.Produce;
import org.joyqueue.broker.producer.ProduceConfig;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.broker.protocol.JoyQueueContext;
import org.joyqueue.broker.protocol.JoyQueueContextAware;
import org.joyqueue.broker.protocol.command.ProduceMessageResponse;
import org.joyqueue.broker.protocol.config.JoyQueueConfig;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.command.ProduceMessageAckData;
import org.joyqueue.network.command.ProduceMessageAckItemData;
import org.joyqueue.network.command.ProduceMessageData;
import org.joyqueue.network.command.ProduceMessageRequest;
import org.joyqueue.network.protocol.annotation.ProduceHandler;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.response.BooleanResponse;
import org.joyqueue.store.WriteResult;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ProduceMessageRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
@ProduceHandler
public class ProduceMessageRequestHandler implements JoyQueueCommandHandler, Type, JoyQueueContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageRequestHandler.class);

    private JoyQueueConfig config;
    private ProduceConfig produceConfig;
    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setJoyQueueContext(JoyQueueContext joyQueueContext) {
        this.config = joyQueueContext.getConfig();
        this.produceConfig = new ProduceConfig(joyQueueContext.getBrokerContext().getPropertySupplier());
        this.produce = joyQueueContext.getBrokerContext().getProduce();
        this.clusterManager = joyQueueContext.getBrokerContext().getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command request) {
        ProduceMessageRequest produceMessageRequest = (ProduceMessageRequest) request.getPayload();
        Connection connection = SessionHelper.getConnection(transport);
        String app = produceMessageRequest.getApp();

        if (connection == null || !connection.isAuthorized(app)) {
            logger.warn("connection does not exist, transport: {}, app: {}", transport, app);
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        QosLevel qosLevel = request.getHeader().getQosLevel();
        CountDownLatch latch = new CountDownLatch(produceMessageRequest.getData().size());
        Map<String, ProduceMessageAckData> resultData = Maps.newConcurrentMap();
        Traffic traffic = new Traffic(app);
        boolean isNeedAck = !qosLevel.equals(QosLevel.ONE_WAY);
        boolean singleTopic = produceMessageRequest.getData().size() == 1;

        for (Map.Entry<String, ProduceMessageData> entry : produceMessageRequest.getData().entrySet()) {
            String topic = entry.getKey();
            ProduceMessageData produceMessageData = entry.getValue();

            try {
                checkAndFillMessage(connection, produceMessageData);
                checkWritable(connection, topic, app, produceMessageData);

                produceMessage(connection, topic, app, produceMessageData, (data) -> {
                    resultData.put(topic, data);
                    traffic.record(topic, produceMessageData.getTraffic(), produceMessageData.getSize());
                    latch.countDown();

                    if (isNeedAck && singleTopic) {
                        transport.acknowledge(request, generateResponse(traffic, resultData));
                    }
                });
            } catch (Exception e) {
                logger.warn("produce message error, transport: {}, topic: {}, app: {}", transport, topic, app, e);
                ProduceMessageAckData produceMessageAckData = null;

                if (e instanceof JoyQueueException) {
                    produceMessageAckData = buildResponse(produceMessageData, JoyQueueCode.valueOf(((JoyQueueException) e).getCode()));
                } else {
                    produceMessageAckData = buildResponse(produceMessageData, JoyQueueCode.CN_UNKNOWN_ERROR);
                }

                latch.countDown();
                resultData.put(topic, produceMessageAckData);

                if (isNeedAck && singleTopic) {
                    transport.acknowledge(request, generateResponse(traffic, resultData));
                    return null;
                }
            }
        }

        if (!isNeedAck || singleTopic) {
            return null;
        }

        try {
            boolean isDone = latch.await(config.getProduceMaxTimeout(), TimeUnit.MILLISECONDS);
            if (!isDone) {
                logger.warn("wait produce timeout, transport: {}, topics: {}", transport.remoteAddress(), produceMessageRequest.getData().keySet());
            }
        } catch (InterruptedException e) {
            logger.error("wait produce exception, transport: {}", transport.remoteAddress(), e);
        }

        return generateResponse(traffic, resultData);
    }

    protected Command generateResponse(Traffic traffic, Map<String, ProduceMessageAckData> resultData) {
        ProduceMessageResponse produceMessageResponse = new ProduceMessageResponse();
        produceMessageResponse.setTraffic(traffic);
        produceMessageResponse.setData(resultData);
        return new Command(produceMessageResponse);
    }

    protected void produceMessage(Connection connection, String topic, String app, ProduceMessageData produceMessageData, EventListener<ProduceMessageAckData> listener) {
        Producer producer = new Producer(connection.getId(), topic, app, Producer.ProducerType.JOYQUEUE);
        try {
            produce.putMessageAsync(producer, produceMessageData.getMessages(), produceMessageData.getQosLevel(), produceMessageData.getTimeout(), (writeResult) -> {
                if (!writeResult.getCode().equals(JoyQueueCode.SUCCESS)) {
                    logger.error("produce message failed, topic: {}, code: {}", producer.getTopic(), writeResult.getCode());
                }
                ProduceMessageAckData produceMessageAckData = new ProduceMessageAckData();
                produceMessageAckData.setCode(writeResult.getCode());
                produceMessageAckData.setItem(buildResponse(produceMessageData.getMessages(), writeResult));
                listener.onEvent(produceMessageAckData);
            });
        } catch (JoyQueueException e) {
            logger.error("produceMessage exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), topic, app, e);
            listener.onEvent(buildResponse(produceMessageData, JoyQueueCode.valueOf(e.getCode())));
        } catch (Exception e) {
            logger.error("produceMessage exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), topic, app, e);
            listener.onEvent(buildResponse(produceMessageData, JoyQueueCode.CN_UNKNOWN_ERROR));
        }
    }

    protected void checkWritable(Connection connection, String topic, String app, ProduceMessageData produceMessageData) throws JoyQueueException {
        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(topic), app, connection.getHost(), produceMessageData.getMessages().get(0).getPartition());
        if (!checkResult.isSuccess()) {
            throw new JoyQueueException(checkResult.getJoyQueueCode());
        }
    }

    protected void checkAndFillMessage(Connection connection, ProduceMessageData produceMessageData) throws JoyQueueException {
        if (CollectionUtils.isEmpty(produceMessageData.getMessages())) {
            throw new JoyQueueException(JoyQueueCode.CN_PARAM_ERROR, "messages not empty");
        }
        byte[] address = connection.getAddress();
        String txId = produceMessageData.getTxId();
        int partition = produceMessageData.getMessages().get(0).getPartition();
        for (BrokerMessage brokerMessage : produceMessageData.getMessages()) {
            if (brokerMessage.getPartition() != partition) {
                throw new JoyQueueException(JoyQueueCode.CN_PARAM_ERROR, "the put message command has multi partition");
            }
            if (ArrayUtils.getLength(brokerMessage.getByteBody()) > produceConfig.getBodyLength()) {
                throw new JoyQueueException(JoyQueueCode.CN_PARAM_ERROR, "message body out of rage");
            }
            if (StringUtils.length(brokerMessage.getBusinessId()) > produceConfig.getBusinessIdLength()) {
                throw new JoyQueueException(JoyQueueCode.CN_PARAM_ERROR, "message businessId out of rage");
            }
            brokerMessage.setClientIp(address);
            brokerMessage.setTxId(txId);
        }
    }

    protected List<ProduceMessageAckItemData> buildResponse(List<BrokerMessage> messages, WriteResult writeResult) {
        BrokerMessage firstMessage = messages.get(0);
        List<ProduceMessageAckItemData> item = Lists.newLinkedList();

        // 批量消息处理
        if (firstMessage.isBatch()) {
            if (ArrayUtils.isEmpty(writeResult.getIndices())) {
                item.add(new ProduceMessageAckItemData(firstMessage.getPartition(), ProduceMessageAckItemData.INVALID_INDEX, firstMessage.getStartTime()));
            } else {
                item.add(new ProduceMessageAckItemData(firstMessage.getPartition(), writeResult.getIndices()[0], firstMessage.getStartTime()));
            }
        } else {
            if (ArrayUtils.isEmpty(writeResult.getIndices())) {
                for (BrokerMessage message : messages) {
                    item.add(new ProduceMessageAckItemData(message.getPartition(), ProduceMessageAckItemData.INVALID_INDEX, message.getStartTime()));
                }
            } else {
                for (int i = 0; i < writeResult.getIndices().length; i++) {
                    BrokerMessage message = messages.get(i);
                    item.add(new ProduceMessageAckItemData(message.getPartition(), writeResult.getIndices()[i], message.getStartTime()));
                }
            }
        }
        return item;
    }

    protected ProduceMessageAckData buildResponse(ProduceMessageData produceMessageData, JoyQueueCode code) {
        BrokerMessage firstMessage = produceMessageData.getMessages().get(0);
        List<ProduceMessageAckItemData> item = Lists.newLinkedList();

        // 批量消息处理
        if (firstMessage.isBatch()) {
            item.add(ProduceMessageAckItemData.INVALID_INSTANCE);
        } else {
            for (int i = 0; i < produceMessageData.getMessages().size(); i++) {
                item.add(ProduceMessageAckItemData.INVALID_INSTANCE);
            }
        }
        return new ProduceMessageAckData(item, code);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_REQUEST.getCode();
    }
}
