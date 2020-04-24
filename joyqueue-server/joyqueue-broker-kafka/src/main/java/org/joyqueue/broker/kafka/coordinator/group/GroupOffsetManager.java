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
package org.joyqueue.broker.kafka.coordinator.group;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.cluster.ClusterNameService;
import org.joyqueue.broker.index.command.ConsumeIndexQueryRequest;
import org.joyqueue.broker.index.command.ConsumeIndexQueryResponse;
import org.joyqueue.broker.index.command.ConsumeIndexStoreRequest;
import org.joyqueue.broker.index.command.ConsumeIndexStoreResponse;
import org.joyqueue.broker.index.model.IndexAndMetadata;
import org.joyqueue.broker.index.model.IndexMetadataAndError;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMetadata;
import org.joyqueue.broker.kafka.model.OffsetAndMetadata;
import org.joyqueue.broker.kafka.model.OffsetMetadataAndError;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.JoyQueueCommand;
import org.joyqueue.network.transport.session.session.TransportSession;
import org.joyqueue.network.transport.session.session.TransportSessionManager;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * GroupOffsetManager
 *
 * author: gaohaoxiang
 * date: 2018/11/7
 */
public class GroupOffsetManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(GroupOffsetManager.class);

    private KafkaConfig config;
    private ClusterNameService clusterNameService;
    private GroupMetadataManager groupMetadataManager;
    private TransportSessionManager sessionManager;

    public GroupOffsetManager(KafkaConfig config, ClusterNameService clusterNameService, GroupMetadataManager groupMetadataManager, TransportSessionManager sessionManager) {
        this.config = config;
        this.clusterNameService = clusterNameService;
        this.groupMetadataManager = groupMetadataManager;
        this.sessionManager = sessionManager;
    }

    public Map<String, List<OffsetMetadataAndError>> getOffsets(String groupId, Map<String, List<Integer>> topicAndPartitions) {
        Map<Broker, Map<String, List<Integer>>> brokerTopicPartitionMap = splitPartitionByBroker(topicAndPartitions);
        CountDownLatch latch = new CountDownLatch(brokerTopicPartitionMap.size());
        Map<String, List<OffsetMetadataAndError>> result = Maps.newHashMapWithExpectedSize(topicAndPartitions.size());

        for (Map.Entry<Broker, Map<String, List<Integer>>> entry : brokerTopicPartitionMap.entrySet()) {
            Broker broker = entry.getKey();

            try {
                TransportSession session = sessionManager.getOrCreateSession(broker);
                ConsumeIndexQueryRequest indexQueryRequest = new ConsumeIndexQueryRequest(groupId, entry.getValue());
                Command request = new JoyQueueCommand(indexQueryRequest);

                session.async(request, config.getOffsetSyncTimeout(), new CommandCallback() {
                    @Override
                    public void onSuccess(Command request, Command response) {
                        synchronized (result) {
                            ConsumeIndexQueryResponse payload = (ConsumeIndexQueryResponse) response.getPayload();
                            for (Map.Entry<String, Map<Integer, IndexMetadataAndError>> topicEntry : payload.getTopicPartitionIndex().entrySet()) {
                                String topic = topicEntry.getKey();
                                List<OffsetMetadataAndError> partitions = result.get(topic);
                                if (partitions == null) {
                                    partitions = Lists.newLinkedList();
                                    result.put(topic, partitions);
                                }

                                for (Map.Entry<Integer, IndexMetadataAndError> partitionEntry : topicEntry.getValue().entrySet()) {
                                    IndexMetadataAndError indexMetadataAndError = partitionEntry.getValue();
                                    partitions.add(new OffsetMetadataAndError(partitionEntry.getKey(), indexMetadataAndError.getIndex(), indexMetadataAndError.getMetadata(),
                                            KafkaErrorCode.joyQueueCodeFor(indexMetadataAndError.getError())));

                                    if (partitionEntry.getValue().getError() != JoyQueueCode.SUCCESS.getCode()) {
                                        logger.error("get offset error, broker: {}, topic: {}, partition: {}, group: {},code: {}",
                                                broker, topic, partitionEntry.getKey(), groupId, JoyQueueCode.valueOf(partitionEntry.getValue().getError()));
                                    }
                                }
                            }
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onException(Command request, Throwable cause) {
                        logger.error("get offset failed, async transport exception, broker: {}, request: {}, group: {}",
                                broker, indexQueryRequest, groupId, cause);

                        synchronized (result) {
                            for (Map.Entry<String, List<Integer>> topicEntry : indexQueryRequest.getTopicPartitions().entrySet()) {
                                String topic = topicEntry.getKey();
                                List<OffsetMetadataAndError> partitions = result.get(topic);
                                if (partitions == null) {
                                    partitions = Lists.newLinkedList();
                                    result.put(topic, partitions);
                                }

                                for (Integer partition : topicEntry.getValue()) {
                                    partitions.add(new OffsetMetadataAndError(partition, OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA,
                                            KafkaErrorCode.NOT_LEADER_FOR_PARTITION.getCode()));
                                }
                            }
                            latch.countDown();
                        }
                    }
                });
            } catch (Throwable cause) {
                logger.error("get offset failed, async transport exception, broker: {}, topic: {}, group: {}",
                        broker, entry.getValue(), groupId, cause);
                latch.countDown();
            }
        }

        try {
            if (!latch.await(config.getOffsetSyncTimeout(), TimeUnit.MILLISECONDS)) {
                logger.error("get offset timeout, partitions: {}, group: {}", topicAndPartitions, groupId);
            }
        } catch (InterruptedException e) {
            logger.error("get offset latch await exception, group: {}, partitions: {}", groupId, topicAndPartitions, e);
        }

        fillErrorOffset(groupId, result);
        return result;
    }

    protected void fillErrorOffset(String groupId, Map<String, List<OffsetMetadataAndError>> result) {
        GroupMetadata groupMetadata = groupMetadataManager.getGroup(groupId);
        if (groupMetadata == null) {
            return;
        }
        for (Map.Entry<String, List<OffsetMetadataAndError>> entry : result.entrySet()) {
            String topic = entry.getKey();
            for (OffsetMetadataAndError offsetMetadataAndError : entry.getValue()) {
                if (offsetMetadataAndError.getError() == KafkaErrorCode.NONE.getCode()) {
                    groupMetadata.putOffsetCache(topic, offsetMetadataAndError.getPartition(),
                            new OffsetAndMetadata(offsetMetadataAndError.getOffset(), (short) offsetMetadataAndError.getPartition()));
                } else {
                    OffsetAndMetadata offsetCache = groupMetadata.getOffsetCache(topic, offsetMetadataAndError.getPartition());
                    if (offsetCache != null) {
                        logger.info("fill error offset, topic: {}, partition: {}, group: {}, offset: {}", topic, entry.getKey(), groupId, offsetCache);
                        offsetMetadataAndError.setOffset(offsetCache.getOffset());
                        offsetMetadataAndError.setMetadata(offsetCache.getMetadata());
                        offsetMetadataAndError.setError(KafkaErrorCode.NONE.getCode());
                    }
                }
            }
        }
    }

    public Map<String, List<OffsetMetadataAndError>> saveOffsets(String groupId, Map<String, List<OffsetAndMetadata>> offsets) {
        Map<Broker, Map<String, List<OffsetAndMetadata>>> brokerTopicPartitionMap = splitOffsetByBroker(offsets);
        CountDownLatch latch = new CountDownLatch(brokerTopicPartitionMap.size());
        Map<String, List<OffsetMetadataAndError>> result = Maps.newHashMapWithExpectedSize(offsets.size());

        for (Map.Entry<Broker, Map<String, List<OffsetAndMetadata>>> entry : brokerTopicPartitionMap.entrySet()) {
            Broker broker = entry.getKey();

            try {
                TransportSession session = sessionManager.getOrCreateSession(broker);
                ConsumeIndexStoreRequest indexStoreRequest = new ConsumeIndexStoreRequest(groupId, buildSaveOffsetParam(entry.getValue()));
                Command request = new JoyQueueCommand(indexStoreRequest);

                session.async(request, config.getOffsetSyncTimeout(), new CommandCallback() {
                    @Override
                    public void onSuccess(Command request, Command response) {
                        synchronized (result) {
                            ConsumeIndexStoreResponse payload = (ConsumeIndexStoreResponse) response.getPayload();
                            for (Map.Entry<String, Map<Integer, Short>> topicEntry : payload.getIndexStoreStatus().entrySet()) {
                                String topic = topicEntry.getKey();
                                List<OffsetMetadataAndError> partitions = result.get(topic);
                                if (partitions == null) {
                                    partitions = Lists.newLinkedList();
                                    result.put(topic, partitions);
                                }

                                for (Map.Entry<Integer, Short> partitionEntry : topicEntry.getValue().entrySet()) {
                                    OffsetMetadataAndError offsetMetadataAndError = new OffsetMetadataAndError(partitionEntry.getKey(), OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA,
                                            KafkaErrorCode.joyQueueCodeFor(partitionEntry.getValue()));

                                    if (partitionEntry.getValue() != JoyQueueCode.SUCCESS.getCode()) {
                                        logger.error("save offset failed, broker: {}, topic: {}, partition: {}, group: {}, code: {}",
                                                broker, topic, partitionEntry.getKey(), groupId, JoyQueueCode.valueOf(partitionEntry.getValue()));
                                    }

                                    offsetMetadataAndError.setError(KafkaErrorCode.NONE.getCode());
                                    partitions.add(offsetMetadataAndError);
                                }
                            }
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onException(Command request, Throwable cause) {
                        logger.error("save offset failed, async transport exception, broker: {}, request: {}, group: {}",
                                broker, indexStoreRequest, groupId, cause);

                        synchronized (result) {
                            for (Map.Entry<String, Map<Integer, IndexAndMetadata>> topicEntry : indexStoreRequest.getIndexMetadata().entrySet()) {
                                String topic = topicEntry.getKey();
                                List<OffsetMetadataAndError> partitions = result.get(topic);
                                if (partitions == null) {
                                    partitions = Lists.newLinkedList();
                                    result.put(topic, partitions);
                                }

                                for (Map.Entry<Integer, IndexAndMetadata> partitionEntry : topicEntry.getValue().entrySet()) {
                                    partitions.add(new OffsetMetadataAndError(partitionEntry.getKey(), OffsetAndMetadata.INVALID_OFFSET,
                                            OffsetAndMetadata.NO_METADATA, KafkaErrorCode.NONE.getCode()));
                                }
                            }
                            latch.countDown();
                        }
                    }
                });
            } catch (Throwable cause) {
                logger.error("save offset failed, async transport exception, broker: {}, topic: {}, group: {}",
                        broker, brokerTopicPartitionMap, groupId, cause);
                latch.countDown();
            }
        }

        try {
            if (!latch.await(config.getOffsetSyncTimeout(), TimeUnit.MILLISECONDS)) {
                logger.error("save offset timeout, offsets: {}, group: {}", brokerTopicPartitionMap, groupId);
            }
        } catch (InterruptedException e) {
            logger.error("save offset latch await exception, group: {}, offsets: {}", groupId, offsets, e);
        }

        fillOffsetCache(groupId, offsets);
        return result;
    }

    protected void fillOffsetCache(String groupId, Map<String, List<OffsetAndMetadata>> result) {
        GroupMetadata groupMetadata = groupMetadataManager.getGroup(groupId);
        if (groupMetadata == null) {
            return;
        }
        for (Map.Entry<String, List<OffsetAndMetadata>> entry : result.entrySet()) {
            for (OffsetAndMetadata offsetAndMetadata : entry.getValue()) {
                groupMetadata.putOffsetCache(entry.getKey(), offsetAndMetadata.getPartition(), offsetAndMetadata);
            }
        }
    }

    protected Map<String, Map<Integer, IndexAndMetadata>> buildSaveOffsetParam(Map<String, List<OffsetAndMetadata>> topicAndPartitions) {
        Map<String, Map<Integer, IndexAndMetadata>> result = Maps.newHashMapWithExpectedSize(topicAndPartitions.size());
        for (Map.Entry<String, List<OffsetAndMetadata>> entry : topicAndPartitions.entrySet()) {
            String topic = entry.getKey();
            Map<Integer, IndexAndMetadata> partitions = Maps.newHashMapWithExpectedSize(entry.getValue().size());
            result.put(topic, partitions);

            for (OffsetAndMetadata offsetAndMetadata : entry.getValue()) {
                partitions.put(offsetAndMetadata.getPartition(), new IndexAndMetadata(offsetAndMetadata.getOffset(), null));
            }
        }
        return result;
    }

    protected Map<Broker, Map<String, List<OffsetAndMetadata>>> splitOffsetByBroker(Map<String, List<OffsetAndMetadata>> offsets) {
        Map<Broker, Map<String, List<OffsetAndMetadata>>> result = Maps.newHashMapWithExpectedSize(offsets.size());
        for (Map.Entry<String, List<OffsetAndMetadata>> entry : offsets.entrySet()) {
            String topic = entry.getKey();
            TopicConfig topicConfig = clusterNameService.getNameService().getTopicConfig(TopicName.parse(topic));
            if (topicConfig == null) {
                logger.error("get leader failed, topic not exist, topic: {}", topic);
                continue;
            }

            for (OffsetAndMetadata offset : entry.getValue()) {
                Broker broker = topicConfig.fetchBrokerByPartition((short) offset.getPartition());
                if (broker == null) {
                    logger.error("get leader failed, topic {}, partition {}, leader not available", topic, offset);
                    continue;
                }

                Map<String, List<OffsetAndMetadata>> brokerTopicAndPartitions = result.get(broker);
                if (brokerTopicAndPartitions == null) {
                    brokerTopicAndPartitions = Maps.newHashMap();
                    result.put(broker, brokerTopicAndPartitions);
                }

                List<OffsetAndMetadata> partitions = brokerTopicAndPartitions.get(topic);
                if (partitions == null) {
                    partitions = Lists.newLinkedList();
                    brokerTopicAndPartitions.put(topic, partitions);
                }

                partitions.add(offset);
            }
        }
        return result;
    }

    protected Map<Broker, Map<String, List<Integer>>> splitPartitionByBroker(Map<String, List<Integer>> topicAndPartitions) {
        Map<Broker, Map<String, List<Integer>>> result = Maps.newHashMapWithExpectedSize(topicAndPartitions.size());

        for (Map.Entry<String, List<Integer>> entry : topicAndPartitions.entrySet()) {
            String topic = entry.getKey();
            TopicConfig topicConfig = clusterNameService.getNameService().getTopicConfig(TopicName.parse(topic));
            if (topicConfig == null) {
                logger.error("get leader failed, topic not exist, topic: {}", topic);
                continue;
            }

            for (Integer partition : entry.getValue()) {
                Broker broker = topicConfig.fetchBrokerByPartition((short) partition.intValue());
                if (broker == null) {
                    logger.error("get leader failed, topic {}, partition {}, leader not available", topic, partition);
                    continue;
                }

                Map<String, List<Integer>> brokerTopicAndPartitions = result.get(broker);
                if (brokerTopicAndPartitions == null) {
                    brokerTopicAndPartitions = Maps.newHashMap();
                    result.put(broker, brokerTopicAndPartitions);
                }

                List<Integer> partitions = brokerTopicAndPartitions.get(topic);
                if (partitions == null) {
                    partitions = Lists.newLinkedList();
                    brokerTopicAndPartitions.put(topic, partitions);
                }

                partitions.add(partition);
            }
        }
        return result;
    }
}