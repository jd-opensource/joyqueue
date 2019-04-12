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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.FetchRequest;
import com.jd.journalq.broker.kafka.command.FetchResponse;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.converter.CheckResultConverter;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.message.converter.KafkaMessageConverter;
import com.jd.journalq.broker.kafka.model.FetchResponsePartitionData;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.response.BooleanResponse;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * FetchHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class FetchHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchHandler.class);

    private KafkaConfig config;
    private Consume consume;
    private ClusterManager clusterManager;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.config = kafkaContext.getConfig();
        this.consume = kafkaContext.getBrokerContext().getConsume();
        this.clusterManager = kafkaContext.getBrokerContext().getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchRequest fetchRequest = (FetchRequest) command.getPayload();
        Table<TopicName, Integer, FetchRequest.PartitionFetchInfo> fetchInfoMap = fetchRequest.getRequestInfo();
        String clientId = KafkaClientHelper.parseClient(fetchRequest.getClientId());
        String clientIp = ((InetSocketAddress) transport.remoteAddress()).getHostString();
        int maxBytes = fetchRequest.getMaxBytes();

        Table<String, Integer, FetchResponsePartitionData> fetchResponseTable = HashBasedTable.create();
        int currentBytes = 0;
        for (TopicName topic : fetchInfoMap.rowKeySet()) {
            for (Map.Entry<Integer, FetchRequest.PartitionFetchInfo> partitionEntry : fetchInfoMap.row(topic).entrySet()) {
                int partition = partitionEntry.getKey();

                if (currentBytes > maxBytes) {
                    fetchResponseTable.put(topic.getFullName(), partition, new FetchResponsePartitionData(KafkaErrorCode.NONE, -1, Collections.emptyList()));
                    continue;
                }

                BooleanResponse checkResult = clusterManager.checkReadable(topic, clientId, clientIp, (short) partition);
                if (!checkResult.isSuccess()) {
                    logger.warn("checkReadable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topic, clientId, checkResult.getJmqCode());
                    short errorCode = CheckResultConverter.convertFetchCode(checkResult.getJmqCode());
                    fetchResponseTable.put(topic.getFullName(), partition, new FetchResponsePartitionData(errorCode, -1, Collections.emptyList()));
                    continue;
                }

                FetchRequest.PartitionFetchInfo partitionFetchInfo = partitionEntry.getValue();
                long offset = partitionFetchInfo.getOffset();
                int partitionMaxBytes = partitionFetchInfo.getMaxBytes();
                FetchResponsePartitionData fetchDataInfo = fetchMessage(transport, topic, partition, clientId, offset, partitionMaxBytes);

                currentBytes += fetchDataInfo.getBytes();
                fetchResponseTable.put(topic.getFullName(), partition, fetchDataInfo);
            }
        }

        FetchResponse fetchResponse = new FetchResponse();
        fetchResponse.setFetchResponses(fetchResponseTable);
        return new Command(fetchResponse);
    }

    private FetchResponsePartitionData fetchMessage(Transport transport, TopicName topic, int partition, String clientId, long offset, int maxBytes) {
        Consumer consumer = new Consumer(clientId, topic.getFullName(), clientId, Consumer.ConsumeType.KAFKA);
        long minIndex = consume.getMinIndex(consumer, (short) partition);
        long maxIndex = consume.getMaxIndex(consumer, (short) partition);
        if (offset < minIndex || offset > maxIndex) {
            logger.warn("fetch message exception, index out of range, transport: {}, consumer: {}, partition: {}, offset: {}, minOffset: {}, maxOffset: {}",
                    transport, consumer, partition, offset, minIndex, maxIndex);
            return new FetchResponsePartitionData(KafkaErrorCode.OFFSET_OUT_OF_RANGE);
        }

        List<KafkaBrokerMessage> kafkaBrokerMessages = Lists.newLinkedList();
        int batchSize = config.getFetchBatchSize();
        int currentBytes = 0;

        // 判断总体长度
        while (currentBytes < maxBytes) {
            List<ByteBuffer> buffers = null;
            try {
                buffers = doFetchMessage(consumer, partition, offset, batchSize);

                if (CollectionUtils.isEmpty(buffers)) {
                    break;
                }

                short skipOffset = 0;
                int currentBatchSize = 0;

                // 消息转换
                for (ByteBuffer buffer : buffers) {
                    currentBytes += (buffer.limit() - buffer.position());
                    BrokerMessage brokerMessage = Serializer.readBrokerMessage(buffer);
                    KafkaBrokerMessage kafkaBrokerMessage = KafkaMessageConverter.toKafkaBrokerMessage(topic.getFullName(), partition, brokerMessage);
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
                short kafkaCode = KafkaErrorCode.kafkaExceptionFor(e);
                if (kafkaCode == KafkaErrorCode.UNKNOWN) {
                    kafkaCode = KafkaErrorCode.NONE;
                }
                return new FetchResponsePartitionData(kafkaCode, offset, kafkaBrokerMessages);
            }
        }

        FetchResponsePartitionData fetchResponsePartitionData = new FetchResponsePartitionData(KafkaErrorCode.NONE, offset, kafkaBrokerMessages);
        fetchResponsePartitionData.setBytes(currentBytes);
        return fetchResponsePartitionData;
    }

    private List<ByteBuffer> doFetchMessage(Consumer consumer, int partition, long offset, int batchSize) throws Exception {
        PullResult pullResult = consume.getMessage(consumer, (short) partition, offset, batchSize);
        if (pullResult.size() == 0) {
            return null;
        }
        if (pullResult.getJmqCode() != JMQCode.SUCCESS) {
            logger.warn("fetch message error, consumer: {}, partition: {}, offset: {}, batchSize: {}", consumer, partition, offset, batchSize);
            return null;
        }
        return pullResult.getBuffers();
    }

    @Override
    public int type() {
        return KafkaCommandType.FETCH.getCode();
    }
}
