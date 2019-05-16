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
package com.jd.journalq.broker.kafka.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.kafka.KafkaAcknowledge;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.ProduceRequest;
import com.jd.journalq.broker.kafka.command.ProduceResponse;
import com.jd.journalq.broker.kafka.converter.CheckResultConverter;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.message.converter.KafkaMessageConverter;
import com.jd.journalq.broker.kafka.model.ProducePartitionStatus;
import com.jd.journalq.broker.network.traffic.Traffic;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JournalqCode;
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
 * ProduceHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/6
 */
public class ProduceHandler extends AbstractKafkaCommandHandler implements BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceRequest produceRequest = (ProduceRequest) command.getPayload();
        KafkaAcknowledge kafkaAcknowledge = KafkaAcknowledge.valueOf(produceRequest.getRequiredAcks());
        QosLevel qosLevel = KafkaAcknowledge.convertToQosLevel(kafkaAcknowledge);
        String clientId = KafkaClientHelper.parseClient(produceRequest.getClientId());
        Traffic traffic = new Traffic(clientId);

        Map<String, List<ProducePartitionStatus>> produceResponseStatusMap = Maps.newHashMap();
        Table<TopicName, Integer, List<KafkaBrokerMessage>> topicPartitionTable = produceRequest.getTopicPartitionMessages();
        CountDownLatch latch = new CountDownLatch(topicPartitionTable.size());
        boolean isNeedAck = !qosLevel.equals(QosLevel.ONE_WAY);
        String clientIp = ((InetSocketAddress) transport.remoteAddress()).getHostString();
        byte[] clientAddress = IpUtil.toByte((InetSocketAddress) transport.remoteAddress());

        for (TopicName topic : topicPartitionTable.rowKeySet()) {
            Map<Integer, List<Integer>> groupPartitionMapper = Maps.newHashMap();
            Map<Integer, List<BrokerMessage>> groupMessageMapper = Maps.newHashMap();
            Map<Integer, List<KafkaBrokerMessage>> partitionMapper = topicPartitionTable.row(topic);
            List<ProducePartitionStatus> producePartitionStatusList = Lists.newLinkedList();
            produceResponseStatusMap.put(topic.getFullName(), producePartitionStatusList);

            Producer producer = new Producer(topic.getFullName(), clientId, Producer.ProducerType.KAFKA);
            TopicConfig topicConfig = clusterManager.getTopicConfig(topic);

            for (Map.Entry<Integer, List<KafkaBrokerMessage>> entry : partitionMapper.entrySet()) {
                int partition = entry.getKey();
                BooleanResponse checkResult = clusterManager.checkWritable(topic, clientId, clientIp, (short) partition);
                if (!checkResult.isSuccess()) {
                    logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topic, clientId, checkResult.getJournalqCode());
                    short kafkaErrorCode = CheckResultConverter.convertProduceCode(checkResult.getJournalqCode());
                    buildPartitionStatus(partition, null, kafkaErrorCode, entry.getValue(), producePartitionStatusList);
                    latch.countDown();
                    continue;
                }

                PartitionGroup partitionGroup = topicConfig.fetchPartitionGroupByPartition((short) partition);
                List<Integer> partitionList = groupPartitionMapper.get(partitionGroup.getGroup());
                List<BrokerMessage> partitionMessageList = groupMessageMapper.get(partitionGroup.getGroup());

                if (partitionList == null) {
                    partitionList = Lists.newLinkedList();
                    groupPartitionMapper.put(partitionGroup.getGroup(), partitionList);
                }

                partitionList.add(partition);

                if (partitionMessageList == null) {
                    partitionMessageList = Lists.newLinkedList();
                    groupMessageMapper.put(partitionGroup.getGroup(), partitionMessageList);
                }

                for (KafkaBrokerMessage message : entry.getValue()) {
                    BrokerMessage brokerMessage = KafkaMessageConverter.toBrokerMessage(producer.getTopic(), partition, producer.getApp(), clientAddress, message);
                    partitionMessageList.add(brokerMessage);
                    traffic.record(topic.getFullName(), brokerMessage.getSize());
                }
            }

            for (Map.Entry<Integer, List<BrokerMessage>> entry : groupMessageMapper.entrySet()) {
                produceMessage(transport, clientAddress, qosLevel, producer, entry.getValue(), (producePartitionStatus) -> {
                    synchronized (producePartitionStatusList) {
                        List<Integer> partitions = groupPartitionMapper.get(entry.getKey());
                        for (Integer partition : partitions) {
                            producePartitionStatusList.add(new ProducePartitionStatus(partition, ProducePartitionStatus.NONE_OFFSET, producePartitionStatus.getErrorCode()));
                            latch.countDown();
                        }
                    }
                });
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

        ProduceResponse response = new ProduceResponse(traffic, produceResponseStatusMap);
        return new Command(response);
    }

    protected void produceMessage(Transport transport, byte[] clientAddress, QosLevel qosLevel, Producer producer, List<BrokerMessage> messages, EventListener<ProducePartitionStatus> listener) {
        try {
            produce.putMessageAsync(producer, messages, qosLevel, (writeResult) -> {
                if (!writeResult.getCode().equals(JournalqCode.SUCCESS)) {
                    logger.error("produce message failed, topic: {}, code: {}", producer.getTopic(), writeResult.getCode());
                }
                short status = KafkaErrorCode.journalqCodeFor(writeResult.getCode().getCode());
                listener.onEvent(new ProducePartitionStatus(0, ProducePartitionStatus.NONE_OFFSET, status));
            });
        } catch (Exception e) {
            logger.error("produce message failed, topic: {}", producer.getTopic(), e);
            short status = KafkaErrorCode.exceptionFor(e);
            listener.onEvent(new ProducePartitionStatus(0, ProducePartitionStatus.NONE_OFFSET, status));
        }
    }

    protected void buildPartitionStatus(int partition, long[] indices, short status, List<KafkaBrokerMessage> messages, List<ProducePartitionStatus> producePartitionStatusList) {
        if (ArrayUtils.isEmpty(indices)) {
            if (messages.get(0).isBatch()) {
                producePartitionStatusList.add(new ProducePartitionStatus(partition, ProducePartitionStatus.NONE_OFFSET, status));
            } else {
                producePartitionStatusList.add(new ProducePartitionStatus(partition, ProducePartitionStatus.NONE_OFFSET, status));
            }
        } else {
            if (messages.get(0).isBatch()) {
                producePartitionStatusList.add(new ProducePartitionStatus(partition, indices[0], status));
            } else {
                producePartitionStatusList.add(new ProducePartitionStatus(partition, indices[0], status));
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }
}