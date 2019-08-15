package io.chubao.joyqueue.broker.kafka.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.broker.kafka.KafkaAcknowledge;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.broker.kafka.KafkaErrorCode;
import io.chubao.joyqueue.broker.kafka.command.ProduceRequest;
import io.chubao.joyqueue.broker.kafka.command.ProduceResponse;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.converter.CheckResultConverter;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.ProducerSequenceManager;
import io.chubao.joyqueue.broker.kafka.helper.KafkaClientHelper;
import io.chubao.joyqueue.broker.kafka.message.KafkaBrokerMessage;
import io.chubao.joyqueue.broker.kafka.message.converter.KafkaMessageConverter;
import io.chubao.joyqueue.broker.kafka.model.ProducePartitionGroupRequest;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.broker.network.traffic.Traffic;
import io.chubao.joyqueue.broker.producer.ProduceConfig;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.message.BrokerMessage;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.session.Producer;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.response.BooleanResponse;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.network.IpUtil;
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
 *
 * author: gaohaoxiang
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
    private KafkaConfig config;

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
            boolean isDone = latch.await(Math.min(produceRequest.getAckTimeoutMs(), config.getProduceTimeout()), TimeUnit.MILLISECONDS);
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

        short checkAndFillMessageResult = checkAndFillMessages(partitionRequest.getMessages());
        if (checkAndFillMessageResult != KafkaErrorCode.NONE.getCode()) {
            return checkAndFillMessageResult;
        }

        BooleanResponse checkResult = clusterManager.checkWritable(topic, producer.getApp(), clientIp, (short) partitionRequest.getPartition());
        if (!checkResult.isSuccess()) {
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
            traffic.record(topic.getFullName(), brokerMessage.getSize());
            brokerMessages.add(brokerMessage);
        }

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