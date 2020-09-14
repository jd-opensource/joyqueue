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
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.MessageConvertSupport;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.command.FetchRequest;
import org.joyqueue.broker.kafka.command.FetchResponse;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.converter.CheckResultConverter;
import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.joyqueue.broker.kafka.message.KafkaBrokerMessage;
import org.joyqueue.broker.kafka.message.converter.KafkaMessageConverter;
import org.joyqueue.broker.monitor.BrokerMonitor;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.SourceType;
import org.joyqueue.network.protocol.annotation.FetchHandler;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.response.BooleanResponse;
import org.joyqueue.toolkit.delay.AbstractDelayedOperation;
import org.joyqueue.toolkit.delay.DelayedOperation;
import org.joyqueue.toolkit.delay.DelayedOperationKey;
import org.joyqueue.toolkit.delay.DelayedOperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * FetchRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
@FetchHandler
public class FetchRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchRequestHandler.class);

    private KafkaConfig config;
    private Consume consume;
    private ClusterManager clusterManager;
    private MessageConvertSupport messageConvertSupport;
    private SessionManager sessionManager;
    private BrokerMonitor brokerMonitor;
    private DelayedOperationManager<DelayedOperation> delayPurgatory;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.config = kafkaContext.getConfig();
        this.consume = kafkaContext.getBrokerContext().getConsume();
        this.clusterManager = kafkaContext.getBrokerContext().getClusterManager();
        this.messageConvertSupport = kafkaContext.getBrokerContext().getMessageConvertSupport();
        this.sessionManager = kafkaContext.getBrokerContext().getSessionManager();
        this.brokerMonitor = kafkaContext.getBrokerContext().getBrokerMonitor();
        this.delayPurgatory = new DelayedOperationManager<>("kafka-fetch-delay");
        this.delayPurgatory.start();
    }

    @Override
    public Command handle(Transport transport, Command request) {
        FetchRequest fetchRequest = (FetchRequest) request.getPayload();
        Connection connection = SessionHelper.getConnection(transport);
        Map<String, List<FetchRequest.PartitionRequest>> partitionRequestMap = fetchRequest.getPartitionRequests();
        String clientId = KafkaClientHelper.parseClient(fetchRequest.getClientId());
        String clientIp = ((InetSocketAddress) transport.remoteAddress()).getHostString();
//        IsolationLevel isolationLevel = IsolationLevel.valueOf(fetchRequest.getIsolationLevel());
        int maxBytes = fetchRequest.getMaxBytes();
        Traffic traffic = new Traffic(clientId);

        Map<String, List<FetchResponse.PartitionResponse>> fetchPartitionResponseMap = Maps.newHashMapWithExpectedSize(partitionRequestMap.size());
        int currentBytes = 0;
        for (Map.Entry<String, List<FetchRequest.PartitionRequest>> entry : partitionRequestMap.entrySet()) {
            TopicName topic = TopicName.parse(entry.getKey());
            List<FetchResponse.PartitionResponse> partitionResponses = Lists.newArrayListWithCapacity(entry.getValue().size());

            String consumerId = connection.getConsumer(topic.getFullName(), clientId);
            Consumer consumer = sessionManager.getConsumerById(consumerId);
            org.joyqueue.domain.Consumer.ConsumerPolicy consumerPolicy = clusterManager.tryGetConsumerPolicy(topic, clientId);

            for (FetchRequest.PartitionRequest partitionRequest : entry.getValue()) {
                int partition = partitionRequest.getPartition();

                if (consumer == null) {
                    partitionResponses.add(new FetchResponse.PartitionResponse(partition, KafkaErrorCode.NOT_LEADER_FOR_PARTITION.getCode()));
                    continue;
                }

                if (fetchRequest.getTraffic().isLimited(topic.getFullName()) || currentBytes > maxBytes) {
                    partitionResponses.add(new FetchResponse.PartitionResponse(partition, KafkaErrorCode.NONE.getCode()));
                    continue;
                }

                BooleanResponse checkResult = clusterManager.checkReadable(topic, clientId, clientIp, (short) partition);
                if (!checkResult.isSuccess()) {
                    logger.warn("checkReadable failed, transport: {}, topic: {}, partition: {}, app: {}, code: {}", transport, topic, partition, clientId, checkResult.getJoyQueueCode());
                    short errorCode = CheckResultConverter.convertFetchCode(checkResult.getJoyQueueCode());
                    partitionResponses.add(new FetchResponse.PartitionResponse(partition, errorCode));
                    continue;
                }

                long offset = partitionRequest.getOffset();
                int partitionMaxBytes = partitionRequest.getMaxBytes();
                FetchResponse.PartitionResponse partitionResponse = fetchMessage(transport, consumer, consumerPolicy, topic, partition, clientId, offset, partitionMaxBytes);

                currentBytes += partitionResponse.getBytes();
                partitionResponses.add(partitionResponse);
                traffic.record(topic.getFullName(), partitionResponse.getBytes(), partitionResponse.getSize());
            }

            fetchPartitionResponseMap.put(entry.getKey(), partitionResponses);
        }

        FetchResponse fetchResponse = new FetchResponse();
        fetchResponse.setPartitionResponses(fetchPartitionResponseMap);
        fetchResponse.setTraffic(traffic);
        Command response = new Command(fetchResponse);

        // 如果没有被限流，并且当前拉取消息量小于最小限制，那么延迟响应
        if (!fetchRequest.getTraffic().isLimited() && fetchRequest.getMinBytes() > currentBytes && fetchRequest.getMaxWait() > 0 && config.getFetchDelay()) {
            delayPurgatory.tryCompleteElseWatch(new AbstractDelayedOperation(fetchRequest.getMaxWait()) {
                @Override
                protected void onComplete() {
                    transport.acknowledge(request, response);
                }
            }, Sets.newHashSet(new DelayedOperationKey()));
            return null;
        }

        return response;
    }

    private FetchResponse.PartitionResponse fetchMessage(Transport transport, Consumer consumer, org.joyqueue.domain.Consumer.ConsumerPolicy consumerPolicy,
                                                         TopicName topic, int partition, String clientId, long offset, int maxBytes) {


        long minIndex = 0;
        long maxIndex = 0;
        try {
            minIndex = consume.getMinIndex(consumer, (short) partition);
            maxIndex = consume.getMaxIndex(consumer, (short) partition);

            if (offset < minIndex || offset > maxIndex) {
                logger.warn("fetch message exception, index out of range, transport: {}, consumer: {}, partition: {}, offset: {}, minOffset: {}, maxOffset: {}",
                        transport, consumer, partition, offset, minIndex, maxIndex);
                return new FetchResponse.PartitionResponse(partition, KafkaErrorCode.OFFSET_OUT_OF_RANGE.getCode());
            }
        } catch (Exception e) {
            logger.error("fetch message exception, check index error, transport: {}, consumer: {}, partition: {}, offset: {}",
                    transport, consumer, partition, offset, e);
            return new FetchResponse.PartitionResponse(partition, KafkaErrorCode.NONE.getCode());
        }

        List<KafkaBrokerMessage> kafkaBrokerMessages = Lists.newLinkedList();
        int batchSize = consumerPolicy.getBatchSize();
        int currentBytes = 0;

        // 判断总体长度
        while (currentBytes < maxBytes && offset < maxIndex) {
            List<BrokerMessage> messages = null;
            try {
                messages = doFetchMessage(consumer, partition, offset, batchSize);

                if (CollectionUtils.isEmpty(messages)) {
                    break;
                }

                short skipOffset = 0;
                int currentBatchSize = 0;

                // 消息转换
                for (BrokerMessage message : messages) {
                    currentBytes += message.getSize();
                    KafkaBrokerMessage kafkaBrokerMessage = KafkaMessageConverter.toKafkaBrokerMessage(topic.getFullName(), partition, message);
                    kafkaBrokerMessages.add(kafkaBrokerMessage);

                    // 如果是批量，跳过批量条数
                    if (kafkaBrokerMessage.isBatch()) {
                        skipOffset += kafkaBrokerMessage.getFlag();
                        currentBatchSize += kafkaBrokerMessage.getFlag();
                    } else {
                        skipOffset += 1;
                        currentBatchSize += 1;
                    }
                }

                // 不满足一批消息量
                if (currentBatchSize < batchSize) {
                    break;
                }

                offset += skipOffset;
            } catch (Exception e) {
                logger.error("fetch message exception, consumer: {}, partition: {}, offset: {}, batchSize: {}", consumer, partition, offset, batchSize, e);
                break;
            }
        }

        FetchResponse.PartitionResponse fetchResponsePartitionData = new FetchResponse.PartitionResponse(partition, KafkaErrorCode.NONE.getCode(), kafkaBrokerMessages);
        fetchResponsePartitionData.setBytes(currentBytes);
        fetchResponsePartitionData.setLogStartOffset(minIndex);
        fetchResponsePartitionData.setLastStableOffset(maxIndex);
        fetchResponsePartitionData.setHighWater(maxIndex);
        return fetchResponsePartitionData;
    }

    private List<BrokerMessage> doFetchMessage(Consumer consumer, int partition, long offset, int batchSize) throws Exception {
        PullResult pullResult = consume.getMessage(consumer, (short) partition, offset, batchSize);
        if (pullResult.getCode() != JoyQueueCode.SUCCESS) {
            logger.warn("fetch message error, consumer: {}, partition: {}, offset: {}, batchSize: {}, code: {}", consumer, partition, offset, batchSize, pullResult.getCode());
            return null;
        }
        if (pullResult.size() == 0) {
            return null;
        }
        List<BrokerMessage> brokerMessages = Lists.newArrayListWithCapacity(pullResult.getBuffers().size());
        for (ByteBuffer buffer : pullResult.getBuffers()) {
            BrokerMessage brokerMessage = Serializer.readBrokerMessage(buffer);
            brokerMessages.add(brokerMessage);
        }
        return messageConvertSupport.convert(brokerMessages, SourceType.KAFKA.getValue());
    }

    @Override
    public int type() {
        return KafkaCommandType.FETCH.getCode();
    }
}
