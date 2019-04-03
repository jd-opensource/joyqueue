package com.jd.journalq.broker.jmq.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.JMQContext;
import com.jd.journalq.broker.jmq.JMQContextAware;
import com.jd.journalq.broker.jmq.config.JMQConfig;
import com.jd.journalq.broker.jmq.converter.CheckResultConverter;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessage;
import com.jd.journalq.network.command.ProduceMessageAck;
import com.jd.journalq.network.command.ProduceMessageAckData;
import com.jd.journalq.network.command.ProduceMessageAckItemData;
import com.jd.journalq.network.command.ProduceMessageData;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import com.jd.journalq.store.WriteResult;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ProduceMessageHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageHandler implements JMQCommandHandler, Type, JMQContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageHandler.class);

    private JMQConfig config;
    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setJmqContext(JMQContext jmqContext) {
        this.config = jmqContext.getConfig();
        this.produce = jmqContext.getBrokerContext().getProduce();
        this.clusterManager= jmqContext.getBrokerContext().getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessage produceMessage = (ProduceMessage) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessage.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        QosLevel qosLevel = command.getHeader().getQosLevel();
        boolean isNeedAck = !qosLevel.equals(QosLevel.ONE_WAY);
        CountDownLatch latch = new CountDownLatch(produceMessage.getData().size());
        Map<String, ProduceMessageAckData> resultData = Maps.newConcurrentMap();

        for (Map.Entry<String, ProduceMessageData> entry : produceMessage.getData().entrySet()) {
            String topic = entry.getKey();
            ProduceMessageData produceMessageData = entry.getValue();

            // 校验
            try {
                checkAndFillMessage(connection, produceMessageData);
            } catch (JMQException e) {
                logger.warn("checkMessage error, transport: {}, topic: {}, app: {}", transport, topic, produceMessage.getApp(), e);
                resultData.put(topic, buildAck(produceMessageData, JMQCode.valueOf(e.getCode())));
                latch.countDown();
                continue;
            }

            BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(topic), produceMessage.getApp(), connection.getHost(), produceMessageData.getMessages().get(0).getPartition());
            if (!checkResult.isSuccess()) {
                logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topic, produceMessage.getApp(), checkResult.getJmqCode());
                resultData.put(topic, buildAck(produceMessageData, CheckResultConverter.convertProduceCode(checkResult.getJmqCode())));
                latch.countDown();
                continue;
            }

            produceMessage(connection, topic, produceMessage.getApp(), produceMessageData, (data) -> {
                resultData.put(topic, data);
                latch.countDown();
            });
        }

        if (!isNeedAck) {
            return null;
        }

        try {
            boolean isDone = latch.await(config.getProduceMaxTimeout(), TimeUnit.MILLISECONDS);
            if (!isDone) {
                logger.warn("wait produce timeout, transport: {}, topics: {}", transport.remoteAddress(), produceMessage.getData().keySet());
            }
        } catch (InterruptedException e) {
            logger.error("wait produce exception, transport: {}", transport.remoteAddress(), e);
        }

        ProduceMessageAck produceMessageAck = new ProduceMessageAck();
        produceMessageAck.setData(resultData);
        return new Command(produceMessageAck);
    }

    protected void produceMessage(Connection connection, String topic, String app, ProduceMessageData produceMessageData, EventListener<ProduceMessageAckData> listener) {
        Producer producer = new Producer(connection.getId(), topic, app, Producer.ProducerType.JMQ);
        try {
            produce.putMessageAsync(producer, produceMessageData.getMessages(), produceMessageData.getQosLevel(), produceMessageData.getTimeout(), (writeResult) -> {
                if (!writeResult.getCode().equals(JMQCode.SUCCESS)) {
                    logger.error("produce message failed, topic: {}, code: {}", producer.getTopic(), writeResult.getCode());
                }
                ProduceMessageAckData produceMessageAckData = new ProduceMessageAckData();
                produceMessageAckData.setCode(writeResult.getCode());
                produceMessageAckData.setItem(buildAck(produceMessageData.getMessages(), writeResult));
                listener.onEvent(produceMessageAckData);
            });
        } catch (JMQException e) {
            logger.error("produceMessage exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), topic, app, e);
            listener.onEvent(buildAck(produceMessageData, JMQCode.valueOf(e.getCode())));
        } catch (Exception e) {
            logger.error("produceMessage exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), topic, app, e);
            listener.onEvent(buildAck(produceMessageData, JMQCode.CN_UNKNOWN_ERROR));
        }
    }

    protected void checkAndFillMessage(Connection connection, ProduceMessageData produceMessageData) throws JMQException {
        if (CollectionUtils.isEmpty(produceMessageData.getMessages())) {
            throw new JMQException(JMQCode.CN_PARAM_ERROR, "messages not empty");
        }
        byte[] address = connection.getAddress();
        String txId = produceMessageData.getTxId();
        int partition = produceMessageData.getMessages().get(0).getPartition();
        for (BrokerMessage brokerMessage : produceMessageData.getMessages()) {
            if (brokerMessage.getPartition() != partition) {
                throw new JMQException(JMQCode.CN_PARAM_ERROR, "the put message command has multi partition");
            }
            brokerMessage.setClientIp(address);
            brokerMessage.setTxId(txId);
        }
    }

    protected List<ProduceMessageAckItemData> buildAck(List<BrokerMessage> messages, WriteResult writeResult) {
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

    protected ProduceMessageAckData buildAck(ProduceMessageData produceMessageData, JMQCode code) {
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
        return JMQCommandType.PRODUCE_MESSAGE.getCode();
    }
}