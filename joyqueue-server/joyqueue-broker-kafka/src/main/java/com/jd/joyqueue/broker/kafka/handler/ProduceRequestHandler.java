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
package com.jd.joyqueue.broker.kafka.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.joyqueue.broker.cluster.ClusterManager;
import com.jd.joyqueue.broker.helper.SessionHelper;
import com.jd.joyqueue.broker.kafka.KafkaAcknowledge;
import com.jd.joyqueue.broker.kafka.KafkaCommandType;
import com.jd.joyqueue.broker.kafka.KafkaContext;
import com.jd.joyqueue.broker.kafka.KafkaContextAware;
import com.jd.joyqueue.broker.kafka.KafkaErrorCode;
import com.jd.joyqueue.broker.kafka.command.ProduceRequest;
import com.jd.joyqueue.broker.kafka.command.ProduceResponse;
import com.jd.joyqueue.broker.kafka.converter.CheckResultConverter;
import com.jd.joyqueue.broker.kafka.coordinator.transaction.ProducerSequenceManager;
import com.jd.joyqueue.broker.kafka.helper.KafkaClientHelper;
import com.jd.joyqueue.broker.kafka.message.KafkaBrokerMessage;
import com.jd.joyqueue.broker.kafka.message.converter.KafkaMessageConverter;
import com.jd.joyqueue.broker.kafka.model.ProducePartitionGroupRequest;
import com.jd.joyqueue.broker.monitor.SessionManager;
import com.jd.joyqueue.broker.network.traffic.Traffic;
import com.jd.joyqueue.broker.producer.ProduceConfig;
import com.jd.joyqueue.domain.PartitionGroup;
import com.jd.joyqueue.domain.QosLevel;
import com.jd.joyqueue.domain.TopicConfig;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.message.BrokerMessage;
import com.jd.joyqueue.network.session.Connection;
import com.jd.joyqueue.network.session.Producer;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.response.BooleanResponse;
import com.jd.joyqueue.toolkit.concurrent.EventListener;
import com.jd.joyqueue.toolkit.network.IpUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ProduceRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/6
 */
public class ProduceRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceRequestHandler.class);

    private ClusterManager clusterManager;
    private ProduceConfig produceConfig;
    private ProduceHandler produceHandler;
    private TransactionProduceHandler transactionProduceHandler;
    private ProducerSequenceManager producerSequenceManager;
    private SessionManager sessionManager;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.clusterManager = kafkaContext.getBrokerContext().getClusterManager();
        this.produceConfig = new ProduceConfig(kafkaContext.getBrokerContext().getPropertySupplier());
        this.produceHandler = new ProduceHandler(kafkaContext.getBrokerContext().getProduce());
        this.transactionProduceHandler = new TransactionProduceHandler(kafkaContext.getConfig(), kafkaContext.getBrokerContext().getProduce(),
                kafkaContext.getTransactionCoordinator(), kafkaContext.getTransactionIdManager());
        this.producerSequenceManager = kafkaContext.getProducerSequenceManager();
        this.sessionManager = kafkaContext.getBrokerContext().getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceRequest produceRequest = (ProduceRequest) command.getPayload();
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

        for (Map.Entry<String, List<ProduceRequest.PartitionRequest>> entry : partitionRequestMap.entrySet()) {
            TopicName topic = TopicName.parse(entry.getKey());
            Map<Integer, ProducePartitionGroupRequest> partitionGroupRequestMap = Maps.newHashMap();
            List<ProduceResponse.PartitionResponse> partitionResponses = Lists.newArrayListWithCapacity(entry.getValue().size());
            partitionResponseMap.put(topic.getFullName(), partitionResponses);

            String producerId = connection.getProducer(topic.getFullName(), clientId);
            Producer producer = sessionManager.getProducerById(producerId);
            TopicConfig topicConfig = clusterManager.getTopicConfig(topic);

            for (ProduceRequest.PartitionRequest partitionRequest : entry.getValue()) {
                short checkCode = checkPartitionRequest(transport, produceRequest, partitionRequest, topic, producer, clientIp);
                if (checkCode != KafkaErrorCode.NONE.getCode()) {
                    buildPartitionResponse(partitionRequest.getPartition(), null, checkCode, partitionRequest.getMessages(), partitionResponses);
                    traffic.record(topic.getFullName(), 0);
                    latch.countDown();
                    continue;
                }
                splitByPartitionGroup(topicConfig, topic, producer, clientAddress, traffic, partitionRequest, partitionGroupRequestMap);
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

        if (!isNeedAck) {
            return null;
        }

        try {
            boolean isDone = latch.await(produceRequest.getAckTimeoutMs(), TimeUnit.MILLISECONDS);
            if (!isDone) {
                logger.warn("wait produce timeout, transport: {}, app: {}, topics: {}", transport.remoteAddress(), clientId, produceRequest.getPartitionRequests().keySet());
            }
        } catch (InterruptedException e) {
            logger.error("wait produce exception, transport: {}, app: {}, topics: {}", transport.remoteAddress(), clientId, produceRequest.getPartitionRequests().keySet(), e);
        }

        ProduceResponse response = new ProduceResponse(traffic, partitionResponseMap);
        return new Command(response);
    }

    protected short checkPartitionRequest(Transport transport, ProduceRequest produceRequest, ProduceRequest.PartitionRequest partitionRequest,
                                          TopicName topic, Producer producer, String clientIp) {

        BooleanResponse checkResult = clusterManager.checkWritable(topic, producer.getApp(), clientIp, (short) partitionRequest.getPartition());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, partition: {}, app: {}, code: {}",
                    transport, topic, partitionRequest.getPartition(), producer.getApp(), checkResult.getJournalqCode());
            return CheckResultConverter.convertProduceCode(checkResult.getJournalqCode());
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
            checkAndFillMessage(brokerMessage);
            traffic.record(topic.getFullName(), brokerMessage.getSize());
            brokerMessages.add(brokerMessage);
        }

        producePartitionGroupRequest.getPartitions().add(partitionRequest.getPartition());
        producePartitionGroupRequest.getMessages().addAll(brokerMessages);
        producePartitionGroupRequest.getMessageMap().put(partitionRequest.getPartition(), brokerMessages);
        producePartitionGroupRequest.getKafkaMessages().addAll(partitionRequest.getMessages());
        producePartitionGroupRequest.getKafkaMessageMap().put(partitionRequest.getPartition(), partitionRequest.getMessages());
    }

    protected void checkAndFillMessage(BrokerMessage message) {
        if (StringUtils.length(message.getBusinessId()) > produceConfig.getBusinessIdLength()) {
            logger.warn("businessId length out of range, topic: {}, app: {}, length: {}", message.getTopic(), message.getApp(), message.getBusinessId().length());
            message.setBusinessId(message.getBusinessId().substring(0, produceConfig.getBusinessIdLength()));
        }
        if (ArrayUtils.getLength(message.getByteBody()) > produceConfig.getBodyLength()) {
            logger.warn("body length out of range, topic: {}, app: {}, length: {}", message.getTopic(), message.getApp(), message.getByteBody().length);
            message.setBody(message.getByteBody(), 0, produceConfig.getBodyLength());
        }
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