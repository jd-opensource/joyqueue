package com.jd.journalq.broker.protocol.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.network.traffic.Traffic;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.broker.producer.ProduceConfig;
import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.broker.protocol.JournalqContext;
import com.jd.journalq.broker.protocol.JournalqContextAware;
import com.jd.journalq.broker.protocol.command.ProduceMessageResponse;
import com.jd.journalq.broker.protocol.config.JournalqConfig;
import com.jd.journalq.broker.protocol.converter.CheckResultConverter;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.command.ProduceMessageAckData;
import com.jd.journalq.network.command.ProduceMessageAckItemData;
import com.jd.journalq.network.command.ProduceMessageData;
import com.jd.journalq.network.command.ProduceMessageRequest;
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
public class ProduceMessageRequestHandler implements JournalqCommandHandler, Type, JournalqContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageRequestHandler.class);

    private JournalqConfig config;
    private ProduceConfig produceConfig;
    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setJournalqContext(JournalqContext journalqContext) {
        this.config = journalqContext.getConfig();
        this.produceConfig = new ProduceConfig(journalqContext.getBrokerContext().getPropertySupplier());
        this.produce = journalqContext.getBrokerContext().getProduce();
        this.clusterManager= journalqContext.getBrokerContext().getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessageRequest produceMessageRequest = (ProduceMessageRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessageRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, produceMessageRequest.getApp());
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
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
            } catch (JournalqException e) {
                logger.warn("checkMessage error, transport: {}, topic: {}, app: {}", transport, topic, produceMessageRequest.getApp(), e);
                resultData.put(topic, buildResponse(produceMessageData, JournalqCode.valueOf(e.getCode())));
                traffic.record(topic, 0);
                latch.countDown();
                continue;
            }

            BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(topic), produceMessageRequest.getApp(),
                    connection.getHost(), produceMessageData.getMessages().get(0).getPartition());
            if (!checkResult.isSuccess()) {
                logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topic, produceMessageRequest.getApp(), checkResult.getJournalqCode());
                resultData.put(topic, buildResponse(produceMessageData, CheckResultConverter.convertProduceCode(checkResult.getJournalqCode())));
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
        Producer producer = new Producer(connection.getId(), topic, app, Producer.ProducerType.JMQ);
        try {
            produce.putMessageAsync(producer, produceMessageData.getMessages(), produceMessageData.getQosLevel(), produceMessageData.getTimeout(), (writeResult) -> {
                if (!writeResult.getCode().equals(JournalqCode.SUCCESS)) {
                    logger.error("produce message failed, topic: {}, code: {}", producer.getTopic(), writeResult.getCode());
                }
                ProduceMessageAckData produceMessageAckData = new ProduceMessageAckData();
                produceMessageAckData.setCode(writeResult.getCode());
                produceMessageAckData.setItem(buildResponse(produceMessageData.getMessages(), writeResult));
                listener.onEvent(produceMessageAckData);
            });
        } catch (JournalqException e) {
            logger.error("produceMessage exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), topic, app, e);
            listener.onEvent(buildResponse(produceMessageData, JournalqCode.valueOf(e.getCode())));
        } catch (Exception e) {
            logger.error("produceMessage exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), topic, app, e);
            listener.onEvent(buildResponse(produceMessageData, JournalqCode.CN_UNKNOWN_ERROR));
        }
    }

    protected void checkAndFillMessage(Connection connection, ProduceMessageData produceMessageData) throws JournalqException {
        if (CollectionUtils.isEmpty(produceMessageData.getMessages())) {
            throw new JournalqException(JournalqCode.CN_PARAM_ERROR, "messages not empty");
        }
        byte[] address = connection.getAddress();
        String txId = produceMessageData.getTxId();
        int partition = produceMessageData.getMessages().get(0).getPartition();
        for (BrokerMessage brokerMessage : produceMessageData.getMessages()) {
            if (brokerMessage.getPartition() != partition) {
                throw new JournalqException(JournalqCode.CN_PARAM_ERROR, "the put message command has multi partition");
            }
            if (StringUtils.length(brokerMessage.getBusinessId()) > produceConfig.getBusinessIdLength()) {
                throw new JournalqException(JournalqCode.CN_PARAM_ERROR, "message businessId out of rage");
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

    protected ProduceMessageAckData buildResponse(ProduceMessageData produceMessageData, JournalqCode code) {
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
        return JournalqCommandType.PRODUCE_MESSAGE_REQUEST.getCode();
    }
}