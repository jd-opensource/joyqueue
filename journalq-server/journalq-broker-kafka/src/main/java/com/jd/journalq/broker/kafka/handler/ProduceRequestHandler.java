package com.jd.journalq.broker.kafka.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.kafka.KafkaAcknowledge;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.command.ProduceRequest;
import com.jd.journalq.broker.kafka.command.ProduceResponse;
import com.jd.journalq.broker.kafka.converter.CheckResultConverter;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.message.converter.KafkaMessageConverter;
import com.jd.journalq.broker.kafka.model.ProducePartitionGroupRequest;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.response.BooleanResponse;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.network.IpUtil;
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
    private ProduceHandler produceHandler;
    private TransactionProduceHandler transactionProduceHandler;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.clusterManager = kafkaContext.getBrokerContext().getClusterManager();
        this.produceHandler = new ProduceHandler(kafkaContext.getBrokerContext().getProduce());
        this.transactionProduceHandler = new TransactionProduceHandler(kafkaContext.getConfig(), kafkaContext.getBrokerContext().getProduce(), kafkaContext.getTransactionCoordinator(), kafkaContext.getTransactionIdManager());
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

        for (Map.Entry<String, List<ProduceRequest.PartitionRequest>> entry : partitionRequestMap.entrySet()) {
            TopicName topicName = TopicName.parse(entry.getKey());
            Map<Integer, ProducePartitionGroupRequest> partitionGroupRequestMap = Maps.newHashMap();
            List<ProduceResponse.PartitionResponse> partitionResponses = Lists.newArrayListWithCapacity(entry.getValue().size());
            partitionResponseMap.put(topicName.getFullName(), partitionResponses);

            Producer producer = new Producer(topicName.getFullName(), clientId, Producer.ProducerType.KAFKA);
            TopicConfig topicConfig = clusterManager.getTopicConfig(topicName);

            for (ProduceRequest.PartitionRequest partitionRequest : entry.getValue()) {
                int partition = partitionRequest.getPartition();
                BooleanResponse checkResult = clusterManager.checkWritable(topicName, clientId, clientIp, (short) partition);

                if (!checkResult.isSuccess()) {
                    logger.warn("checkWritable failed, transport: {}, topic: {}, partition: {}, app: {}, code: {}", transport, topicName, partition, clientId, checkResult.getJournalqCode());
                    short kafkaErrorCode = CheckResultConverter.convertProduceCode(checkResult.getJournalqCode());
                    buildPartitionResponse(partition, null, kafkaErrorCode, partitionRequest.getMessages(), partitionResponses);
                    latch.countDown();
                    continue;
                }

                PartitionGroup partitionGroup = topicConfig.fetchPartitionGroupByPartition((short) partition);
                ProducePartitionGroupRequest producePartitionGroupRequest = partitionGroupRequestMap.get(partitionGroup.getGroup());

                if (producePartitionGroupRequest == null) {
                    producePartitionGroupRequest = new ProducePartitionGroupRequest(Lists.newLinkedList(), Lists.newLinkedList(), Maps.newHashMap());
                    partitionGroupRequestMap.put(partitionGroup.getGroup(), producePartitionGroupRequest);
                }

                List<BrokerMessage> brokerMessages = Lists.newLinkedList();
                for (KafkaBrokerMessage message : partitionRequest.getMessages()) {
                    BrokerMessage brokerMessage = KafkaMessageConverter.toBrokerMessage(producer.getTopic(), partition, producer.getApp(), clientAddress, message);
                    brokerMessages.add(brokerMessage);
                }

                producePartitionGroupRequest.getPartitions().add(partition);
                producePartitionGroupRequest.getMessages().addAll(brokerMessages);
                producePartitionGroupRequest.getMessageMap().put(partitionRequest.getPartition(), brokerMessages);
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
                    transactionProduceHandler.produceMessage(produceRequest.getTransactionalId(), produceRequest.getProducerId(), produceRequest.getProducerEpoch(),
                            qosLevel, producer, partitionGroupEntry.getValue(), listener);
                } else {
                    produceHandler.produceMessage(qosLevel, producer, partitionGroupEntry.getValue(), listener);
                }
            }
        }

        if (!isNeedAck) {
            return null;
        }

        try {
            boolean isDone = latch.await(produceRequest.getAckTimeoutMs(), TimeUnit.MILLISECONDS);
            if (!isDone) {
                logger.warn("wait produce timeout, transport: {}", transport.remoteAddress());
            }
        } catch (InterruptedException e) {
            logger.error("wait produce exception, transport: {}", transport.remoteAddress(), e);
        }

        ProduceResponse response = new ProduceResponse(partitionResponseMap);
        return new Command(response);
    }

    protected void buildPartitionResponse(int partition, long[] indices, short code, List<KafkaBrokerMessage> messages, List<ProduceResponse.PartitionResponse> partitionResponses) {
        if (ArrayUtils.isEmpty(indices)) {
            if (messages.get(0).isBatch()) {
                partitionResponses.add(new ProduceResponse.PartitionResponse(partition, ProduceResponse.PartitionResponse.NONE_OFFSET, code));
            } else {
                partitionResponses.add(new ProduceResponse.PartitionResponse(partition, ProduceResponse.PartitionResponse.NONE_OFFSET, code));
            }
        } else {
            if (messages.get(0).isBatch()) {
                partitionResponses.add(new ProduceResponse.PartitionResponse(partition, indices[0], code));
            } else {
                partitionResponses.add(new ProduceResponse.PartitionResponse(partition, indices[0], code));
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }
}