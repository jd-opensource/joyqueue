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
import com.google.common.collect.Sets;
import com.jd.joyqueue.broker.buffer.Serializer;
import com.jd.joyqueue.broker.cluster.ClusterManager;
import com.jd.joyqueue.broker.consumer.Consume;
import com.jd.joyqueue.broker.consumer.MessageConvertSupport;
import com.jd.joyqueue.broker.consumer.model.PullResult;
import com.jd.joyqueue.broker.helper.SessionHelper;
import com.jd.joyqueue.broker.kafka.KafkaCommandType;
import com.jd.joyqueue.broker.kafka.KafkaContext;
import com.jd.joyqueue.broker.kafka.KafkaContextAware;
import com.jd.joyqueue.broker.kafka.KafkaErrorCode;
import com.jd.joyqueue.broker.kafka.command.FetchRequest;
import com.jd.joyqueue.broker.kafka.command.FetchResponse;
import com.jd.joyqueue.broker.kafka.config.KafkaConfig;
import com.jd.joyqueue.broker.kafka.converter.CheckResultConverter;
import com.jd.joyqueue.broker.kafka.helper.KafkaClientHelper;
import com.jd.joyqueue.broker.kafka.message.KafkaBrokerMessage;
import com.jd.joyqueue.broker.kafka.message.converter.KafkaMessageConverter;
import com.jd.joyqueue.broker.monitor.SessionManager;
import com.jd.joyqueue.broker.network.traffic.Traffic;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.exception.JoyQueueCode;
import com.jd.joyqueue.message.BrokerMessage;
import com.jd.joyqueue.message.SourceType;
import com.jd.joyqueue.network.session.Connection;
import com.jd.joyqueue.network.session.Consumer;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.response.BooleanResponse;
import com.jd.joyqueue.toolkit.delay.AbstractDelayedOperation;
import com.jd.joyqueue.toolkit.delay.DelayedOperation;
import com.jd.joyqueue.toolkit.delay.DelayedOperationKey;
import com.jd.joyqueue.toolkit.delay.DelayedOperationManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * FetchRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class FetchRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchRequestHandler.class);

    private KafkaConfig config;
    private Consume consume;
    private ClusterManager clusterManager;
    private MessageConvertSupport messageConvertSupport;
    private SessionManager sessionManager;
    private DelayedOperationManager<DelayedOperation> delayPurgatory;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.config = kafkaContext.getConfig();
        this.consume = kafkaContext.getBrokerContext().getConsume();
        this.clusterManager = kafkaContext.getBrokerContext().getClusterManager();
        this.messageConvertSupport = kafkaContext.getBrokerContext().getMessageConvertSupport();
        this.sessionManager = kafkaContext.getBrokerContext().getSessionManager();
        this.delayPurgatory = new DelayedOperationManager<>("kafka-fetch-wait");
        this.delayPurgatory.start();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchRequest fetchRequest = (FetchRequest) command.getPayload();
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

            for (FetchRequest.PartitionRequest partitionRequest : entry.getValue()) {
                int partition = partitionRequest.getPartition();

                if (currentBytes > maxBytes) {
                    partitionResponses.add(new FetchResponse.PartitionResponse(partition, KafkaErrorCode.NONE.getCode()));
                    continue;
                }

                BooleanResponse checkResult = clusterManager.checkReadable(topic, clientId, clientIp, (short) partition);
                if (!checkResult.isSuccess()) {
                    logger.warn("checkReadable failed, transport: {}, topic: {}, partition: {}, app: {}, code: {}", transport, topic, partition, clientId, checkResult.getJoyQueueCode());
                    short errorCode = CheckResultConverter.convertFetchCode(checkResult.getJoyQueueCode());
                    partitionResponses.add(new FetchResponse.PartitionResponse(partition, errorCode));
                    traffic.record(topic.getFullName(), 0);
                    continue;
                }

                long offset = partitionRequest.getOffset();
                int partitionMaxBytes = partitionRequest.getMaxBytes();
                FetchResponse.PartitionResponse partitionResponse = fetchMessage(transport, topic, partition, clientId, offset, partitionMaxBytes);

                currentBytes += partitionResponse.getBytes();
                partitionResponses.add(partitionResponse);
                traffic.record(topic.getFullName(), partitionResponse.getBytes());
            }

            fetchPartitionResponseMap.put(entry.getKey(), partitionResponses);
        }

        FetchResponse fetchResponse = new FetchResponse();
        fetchResponse.setPartitionResponses(fetchPartitionResponseMap);
        Command response = new Command(fetchResponse);

        // 如果当前拉取消息量小于最小限制，那么延迟响应
        if (fetchRequest.getMinBytes() > currentBytes && fetchRequest.getMaxWait() > 0) {
            delayPurgatory.tryCompleteElseWatch(new AbstractDelayedOperation(fetchRequest.getMaxWait()) {
                @Override
                protected void onComplete() {
                    transport.acknowledge(command, response);
                }
            }, Sets.newHashSet(new DelayedOperationKey()));
            return null;
        }

        return response;
    }

    private FetchResponse.PartitionResponse fetchMessage(Transport transport, TopicName topic, int partition, String clientId, long offset, int maxBytes) {
        Connection connection = SessionHelper.getConnection(transport);
        String consumerId = connection.getConsumer(topic.getFullName(), clientId);
        Consumer consumer = sessionManager.getConsumerById(consumerId);
        long minIndex = consume.getMinIndex(consumer, (short) partition);
        long maxIndex = consume.getMaxIndex(consumer, (short) partition);

        if (offset < minIndex || offset > maxIndex) {
            logger.warn("fetch message exception, index out of range, transport: {}, consumer: {}, partition: {}, offset: {}, minOffset: {}, maxOffset: {}",
                    transport, consumer, partition, offset, minIndex, maxIndex);
            return new FetchResponse.PartitionResponse(partition, KafkaErrorCode.OFFSET_OUT_OF_RANGE.getCode());
        }

        List<KafkaBrokerMessage> kafkaBrokerMessages = Lists.newLinkedList();
        int batchSize = config.getFetchBatchSize();
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
