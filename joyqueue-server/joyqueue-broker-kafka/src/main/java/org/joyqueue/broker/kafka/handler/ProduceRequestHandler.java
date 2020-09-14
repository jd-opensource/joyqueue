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
package org.joyqueue.broker.kafka.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ArrayUtils;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.kafka.KafkaAcknowledge;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.command.ProduceRequest;
import org.joyqueue.broker.kafka.command.ProduceResponse;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.converter.CheckResultConverter;
import org.joyqueue.broker.kafka.coordinator.transaction.ProducerSequenceManager;
import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.joyqueue.broker.kafka.message.KafkaBrokerMessage;
import org.joyqueue.broker.kafka.message.converter.KafkaMessageConverter;
import org.joyqueue.broker.kafka.model.ProducePartitionGroupRequest;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.broker.producer.ProduceConfig;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.response.BooleanResponse;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.delay.AbstractDelayedOperation;
import org.joyqueue.toolkit.delay.DelayedOperation;
import org.joyqueue.toolkit.delay.DelayedOperationKey;
import org.joyqueue.toolkit.delay.DelayedOperationManager;
import org.joyqueue.toolkit.network.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ProduceRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/6
 */
@org.joyqueue.network.protocol.annotation.ProduceHandler
public class ProduceRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceRequestHandler.class);

    private ClusterManager clusterManager;
    private ProduceConfig produceConfig;
    private ProduceHandler produceHandler;
    private TransactionProduceHandler transactionProduceHandler;
    private ProducerSequenceManager producerSequenceManager;
    private SessionManager sessionManager;
    private KafkaConfig config;
    private DelayedOperationManager<DelayedOperation> delayPurgatory;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.clusterManager = kafkaContext.getBrokerContext().getClusterManager();
        this.produceConfig = new ProduceConfig(kafkaContext.getBrokerContext().getPropertySupplier());
        this.produceHandler = new ProduceHandler(kafkaContext.getBrokerContext().getProduce());
        this.transactionProduceHandler = new TransactionProduceHandler(kafkaContext.getConfig(), kafkaContext.getBrokerContext().getProduce(),
                kafkaContext.getTransactionCoordinator(), kafkaContext.getTransactionIdManager());
        this.producerSequenceManager = kafkaContext.getProducerSequenceManager();
        this.sessionManager = kafkaContext.getBrokerContext().getSessionManager();
        this.config = kafkaContext.getConfig();
        this.delayPurgatory = new DelayedOperationManager<>("kafka-produce-delay");
        this.delayPurgatory.start();
    }

    @Override
    public Command handle(Transport transport, Command request) {
        ProduceRequest produceRequest = (ProduceRequest) request.getPayload();
        KafkaAcknowledge kafkaAcknowledge = KafkaAcknowledge.valueOf(produceRequest.getRequiredAcks());
        QosLevel qosLevel = KafkaAcknowledge.convertToQosLevel(kafkaAcknowledge);
        String clientId = KafkaClientHelper.parseClient(produceRequest.getClientId());
        Map<String, List<ProduceRequest.PartitionRequest>> partitionRequestMap = produceRequest.getPartitionRequests();

        Map<String, List<ProduceResponse.PartitionResponse>> partitionResponseMap = Maps.newHashMapWithExpectedSize(partitionRequestMap.size());
        CountDownLatch latch = new CountDownLatch(produceRequest.getPartitionNum());
        boolean isNeedAck = !qosLevel.equals(QosLevel.ONE_WAY);
        String clientIp = ((InetSocketAddress) transport.remoteAddress()).getHostString();
        byte[] clientAddress = IpUtil.toByte((InetSocketAddress) transport.remoteAddress());
        Connection connection = SessionHelper.getConnection(transport);
        Traffic traffic = new Traffic(clientId);
        boolean[] isNeedDelay = {false};
        boolean[] singleTopic = {partitionRequestMap.size() == 1};

        for (Map.Entry<String, List<ProduceRequest.PartitionRequest>> partitionRequestEntry : partitionRequestMap.entrySet()) {
            TopicName topic = TopicName.parse(partitionRequestEntry.getKey());
            Map<Integer, ProducePartitionGroupRequest> partitionGroupRequestMap = Maps.newHashMap();
            List<ProduceResponse.PartitionResponse> partitionResponses = Lists.newArrayListWithCapacity(partitionRequestEntry.getValue().size());
            partitionResponseMap.put(topic.getFullName(), partitionResponses);

            String producerId = connection.getProducer(topic.getFullName(), clientId);
            Producer producer = sessionManager.getProducerById(producerId);
            TopicConfig topicConfig = clusterManager.getTopicConfig(topic);

            for (ProduceRequest.PartitionRequest partitionRequest : partitionRequestEntry.getValue()) {
                if (producer == null) {
                    buildPartitionResponse(partitionRequest.getPartition(), null, KafkaErrorCode.NOT_LEADER_FOR_PARTITION.getCode(), partitionRequest.getMessages(), partitionResponses);
                    latch.countDown();
                    isNeedDelay[0] = true;
                    continue;
                }

                short checkCode = checkPartitionRequest(transport, produceRequest, partitionRequest, topic, producer, clientIp);
                if (checkCode != KafkaErrorCode.NONE.getCode()) {
                    buildPartitionResponse(partitionRequest.getPartition(), null, checkCode, partitionRequest.getMessages(), partitionResponses);
                    latch.countDown();
                    isNeedDelay[0] = true;
                    continue;
                }
                splitByPartitionGroup(topicConfig, topic, producer, clientAddress, traffic, partitionRequest, partitionGroupRequestMap);
            }

            boolean singleGroup = (partitionGroupRequestMap.size() == 1);

            if (singleTopic[0] && !singleGroup) {
                singleTopic[0] = false;
            }

            if (singleTopic[0] && isNeedAck && partitionResponses.size() == partitionRequestEntry.getValue().size()) {
                return delayResponse(transport, request, generateResponse(traffic, partitionResponseMap));
            }

            for (Map.Entry<Integer, ProducePartitionGroupRequest> partitionGroupEntry : partitionGroupRequestMap.entrySet()) {
                EventListener<ProduceResponse.PartitionResponse> listener = new EventListener<ProduceResponse.PartitionResponse>() {
                    @Override
                    public void onEvent(ProduceResponse.PartitionResponse produceResponse) {
                        List<Integer> partitions = partitionGroupEntry.getValue().getPartitions();
                        synchronized (partitionResponses) {
                            for (Integer partition : partitions) {
                                partitionResponses.add(new ProduceResponse.PartitionResponse(partition, ProduceResponse.PartitionResponse.NONE_OFFSET, produceResponse.getErrorCode()));
                                latch.countDown();
                            }
                        }
                        if (produceResponse.getErrorCode() != KafkaErrorCode.NONE.getCode()) {
                            isNeedDelay[0] = true;
                        }

                        if (isNeedAck && singleTopic[0]) {
                            Command response = null;
                            if (isNeedDelay[0]) {
                                response = delayResponse(transport, request, generateResponse(traffic, partitionResponseMap));
                            } else {
                                response = generateResponse(traffic, partitionResponseMap);
                            }

                            if (response != null) {
                                transport.acknowledge(request, response);
                            }
                        }
                    }
                };

                if (produceRequest.isTransaction()) {
                    transactionProduceHandler.produceMessage(produceRequest, produceRequest.getTransactionalId(), produceRequest.getProducerId(), produceRequest.getProducerEpoch(),
                            qosLevel, producer, partitionGroupEntry.getValue(), listener);
                } else {
                    produceHandler.produceMessage(produceRequest, qosLevel, producer, partitionGroupEntry.getValue(), listener);
                }
            }
        }

        if (!isNeedAck || singleTopic[0]) {
            return null;
        }

        try {
            boolean isDone = latch.await(Math.min(produceRequest.getAckTimeoutMs(), config.getProduceTimeout()), TimeUnit.MILLISECONDS);
            if (!isDone) {
                isNeedDelay[0] = true;
                logger.warn("wait produce timeout, transport: {}, app: {}, topics: {}", transport.remoteAddress(), clientId, produceRequest.getPartitionRequests().keySet());
            }
        } catch (InterruptedException e) {
            logger.error("wait produce exception, transport: {}, app: {}, topics: {}", transport.remoteAddress(), clientId, produceRequest.getPartitionRequests().keySet(), e);
        }

        Command response = generateResponse(traffic, partitionResponseMap);
        if (isNeedDelay[0]) {
            return delayResponse(transport, request, response);
        } else {
            return response;
        }
    }

    protected Command delayResponse(Transport transport, Command request, Command response) {
        if (config.getProduceDelayEnable()) {
            return response;
        }
        delayPurgatory.tryCompleteElseWatch(new AbstractDelayedOperation(config.getProduceDelay()) {
            @Override
            protected void onComplete() {
                transport.acknowledge(request, response);
            }
        }, Sets.newHashSet(new DelayedOperationKey()));
        return null;
    }

    protected Command generateResponse(Traffic traffic, Map<String, List<ProduceResponse.PartitionResponse>> partitionResponseMap) {
        ProduceResponse produceResponse = new ProduceResponse(traffic, partitionResponseMap);
        return new Command(produceResponse);
    }

    protected short checkPartitionRequest(Transport transport, ProduceRequest produceRequest, ProduceRequest.PartitionRequest partitionRequest,
                                          TopicName topic, Producer producer, String clientIp) {

        short checkAndFillMessageResult = checkAndFillMessages(partitionRequest.getMessages());
        if (checkAndFillMessageResult != KafkaErrorCode.NONE.getCode()) {
            return checkAndFillMessageResult;
        }

        BooleanResponse checkResult = clusterManager.checkWritable(topic, producer.getApp(), clientIp, (short) partitionRequest.getPartition());
        if (!checkResult.isSuccess() && !checkResult.getJoyQueueCode().equals(JoyQueueCode.FW_BROKER_NOT_WRITABLE)) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, partition: {}, app: {}, code: {}",
                    transport, topic, partitionRequest.getPartition(), producer.getApp(), checkResult.getJoyQueueCode());
            return CheckResultConverter.convertProduceCode(checkResult.getJoyQueueCode());
        }

        int baseSequence = partitionRequest.getMessages().get(0).getBaseSequence();
        if (baseSequence != KafkaBrokerMessage.NO_SEQUENCE) {
            if (!producerSequenceManager.checkSequence(producer.getApp(), produceRequest.getProducerId(), produceRequest.getProducerEpoch(), partitionRequest.getPartition(), baseSequence)) {
                logger.warn("out of order sequence, topic: {}, app: {}, partition: {}, transactionId: {}, producerId: {}, producerEpoch: {}, sequence: {}",
                        producer.getTopic(), producer.getApp(), partitionRequest.getPartition(), produceRequest.getTransactionalId(),
                        produceRequest.getProducerId(), produceRequest.getProducerEpoch(), baseSequence);

                return KafkaErrorCode.OUT_OF_ORDER_SEQUENCE_NUMBER.getCode();
            } else {
                producerSequenceManager.updateSequence(producer.getApp(), produceRequest.getProducerId(), produceRequest.getProducerEpoch(), partitionRequest.getPartition(), baseSequence);
            }
        }

        return KafkaErrorCode.NONE.getCode();
    }

    protected void splitByPartitionGroup(TopicConfig topicConfig, TopicName topic, Producer producer, byte[] clientAddress, Traffic traffic,
                                ProduceRequest.PartitionRequest partitionRequest, Map<Integer, ProducePartitionGroupRequest> partitionGroupRequestMap) {
        PartitionGroup partitionGroup = topicConfig.fetchPartitionGroupByPartition((short) partitionRequest.getPartition());
        ProducePartitionGroupRequest producePartitionGroupRequest = partitionGroupRequestMap.get(partitionGroup.getGroup());

        if (producePartitionGroupRequest == null) {
            producePartitionGroupRequest = new ProducePartitionGroupRequest(Lists.newLinkedList(), Lists.newLinkedList(),
                    Lists.newLinkedList(), Maps.newHashMap(), Maps.newHashMap());
            partitionGroupRequestMap.put(partitionGroup.getGroup(), producePartitionGroupRequest);
        }

        List<BrokerMessage> brokerMessages = Lists.newLinkedList();
        for (KafkaBrokerMessage message : partitionRequest.getMessages()) {
            BrokerMessage brokerMessage = KafkaMessageConverter.toBrokerMessage(producer.getTopic(), partitionRequest.getPartition(), producer.getApp(), clientAddress, message);
            brokerMessages.add(brokerMessage);
        }

        traffic.record(topic.getFullName(), partitionRequest.getTraffic(), partitionRequest.getSize());
        producePartitionGroupRequest.getPartitions().add(partitionRequest.getPartition());
        producePartitionGroupRequest.getMessages().addAll(brokerMessages);
        producePartitionGroupRequest.getMessageMap().put(partitionRequest.getPartition(), brokerMessages);
        producePartitionGroupRequest.getKafkaMessages().addAll(partitionRequest.getMessages());
        producePartitionGroupRequest.getKafkaMessageMap().put(partitionRequest.getPartition(), partitionRequest.getMessages());
    }

    protected short checkAndFillMessages(List<KafkaBrokerMessage> messages) {
        for (KafkaBrokerMessage message : messages) {
            if (ArrayUtils.getLength(message.getKey()) > produceConfig.getBusinessIdLength()) {
                return KafkaErrorCode.MESSAGE_TOO_LARGE.getCode();
            }
            if (ArrayUtils.getLength(message.getValue()) > produceConfig.getBodyLength()) {
                return KafkaErrorCode.MESSAGE_TOO_LARGE.getCode();
            }
        }
        return KafkaErrorCode.NONE.getCode();
    }

    protected void buildPartitionResponse(int partition, long[] indices, short code, List<KafkaBrokerMessage> messages, List<ProduceResponse.PartitionResponse> partitionResponses) {
        if (ArrayUtils.isEmpty(indices)) {
            partitionResponses.add(new ProduceResponse.PartitionResponse(partition, ProduceResponse.PartitionResponse.NONE_OFFSET, code));
        } else {
            partitionResponses.add(new ProduceResponse.PartitionResponse(partition, indices[0], code));
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }
}