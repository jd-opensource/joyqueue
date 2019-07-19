/**
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
package com.jd.joyqueue.broker.protocol.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.joyqueue.broker.cluster.ClusterManager;
import com.jd.joyqueue.broker.helper.SessionHelper;
import com.jd.joyqueue.broker.network.traffic.Traffic;
import com.jd.joyqueue.broker.producer.Produce;
import com.jd.joyqueue.broker.producer.ProduceConfig;
import com.jd.joyqueue.broker.protocol.JoyQueueCommandHandler;
import com.jd.joyqueue.broker.protocol.JoyQueueContext;
import com.jd.joyqueue.broker.protocol.JoyQueueContextAware;
import com.jd.joyqueue.broker.protocol.command.ProduceMessageResponse;
import com.jd.joyqueue.broker.protocol.config.JoyQueueConfig;
import com.jd.joyqueue.broker.protocol.converter.CheckResultConverter;
import com.jd.joyqueue.domain.QosLevel;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.exception.JoyQueueCode;
import com.jd.joyqueue.exception.JoyQueueException;
import com.jd.joyqueue.message.BrokerMessage;
import com.jd.joyqueue.network.command.BooleanAck;
import com.jd.joyqueue.network.command.JoyQueueCommandType;
import com.jd.joyqueue.network.command.ProduceMessageAckData;
import com.jd.joyqueue.network.command.ProduceMessageAckItemData;
import com.jd.joyqueue.network.command.ProduceMessageData;
import com.jd.joyqueue.network.command.ProduceMessageRequest;
import com.jd.joyqueue.network.session.Connection;
import com.jd.joyqueue.network.session.Producer;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Type;
import com.jd.joyqueue.response.BooleanResponse;
import com.jd.joyqueue.store.WriteResult;
import com.jd.joyqueue.toolkit.concurrent.EventListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ProduceMessageRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
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
    public Command handle(Transport transport, Command command) {
        ProduceMessageRequest produceMessageRequest = (ProduceMessageRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessageRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, produceMessageRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        QosLevel qosLevel = command.getHeader().getQosLevel();
        boolean isNeedAck = !qosLevel.equals(QosLevel.ONE_WAY);
        CountDownLatch latch = new CountDownLatch(produceMessageRequest.getData().size());
        Map<String, ProduceMessageAckData> resultData = Maps.newConcurrentMap();
        Traffic traffic = new Traffic(produceMessageRequest.getApp());

        for (Map.Entry<String, ProduceMessageData> entry : produceMessageRequest.getData().entrySet()) {
            String topic = entry.getKey();
            ProduceMessageData produceMessageData = entry.getValue();

            // 校验
            try {
                checkAndFillMessage(connection, produceMessageData);
            } catch (JoyQueueException e) {
                logger.warn("checkMessage error, transport: {}, topic: {}, app: {}", transport, topic, produceMessageRequest.getApp(), e);
                resultData.put(topic, buildResponse(produceMessageData, JoyQueueCode.valueOf(e.getCode())));
                traffic.record(topic, 0);
                latch.countDown();
                continue;
            }

            BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(topic), produceMessageRequest.getApp(),
                    connection.getHost(), produceMessageData.getMessages().get(0).getPartition());
            if (!checkResult.isSuccess()) {
                logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topic, produceMessageRequest.getApp(), checkResult.getJoyQueueCode());
                resultData.put(topic, buildResponse(produceMessageData, CheckResultConverter.convertProduceCode(checkResult.getJoyQueueCode())));
                traffic.record(topic, 0);
                latch.countDown();
                continue;
            }

            produceMessage(connection, topic, produceMessageRequest.getApp(), produceMessageData, (data) -> {
                resultData.put(topic, data);
                traffic.record(topic, produceMessageData.getSize());
                latch.countDown();
            });
        }

        if (!isNeedAck) {
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
                for (int i = 0; i < firstMessage.getFlag(); i++) {
                    item.add(new ProduceMessageAckItemData(firstMessage.getPartition(), ProduceMessageAckItemData.INVALID_INDEX, firstMessage.getStartTime()));
                }
            } else {
                for (int i = 0; i < writeResult.getIndices().length; i++) {
                    item.add(new ProduceMessageAckItemData(firstMessage.getPartition(), writeResult.getIndices()[i], firstMessage.getStartTime()));
                }
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
            for (int i = 0; i < firstMessage.getFlag(); i++) {
                item.add(ProduceMessageAckItemData.INVALID_INSTANCE);
            }
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