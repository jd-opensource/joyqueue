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
package org.joyqueue.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.cluster.ClusterNameService;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.coordinator.Coordinator;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionState;
import org.joyqueue.broker.kafka.coordinator.transaction.exception.TransactionException;
import org.joyqueue.broker.kafka.coordinator.transaction.synchronizer.TransactionSynchronizer;
import org.joyqueue.broker.kafka.model.PartitionMetadataAndError;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TransactionHandler
 *
 * author: gaohaoxiang
 * date: 2019/4/10
 */
public class TransactionHandler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionHandler.class);

    private static final String IDEMPOTENCE_TRANSACTION_ID_SUFFIX = "__SUFFIX_";

    private Coordinator coordinator;
    private TransactionMetadataManager transactionMetadataManager;
    private ProducerIdManager producerIdManager;
    private TransactionSynchronizer transactionSynchronizer;
    private ClusterNameService clusterNameService;

    public TransactionHandler(Coordinator coordinator, TransactionMetadataManager transactionMetadataManager, ProducerIdManager producerIdManager,
                              TransactionSynchronizer transactionSynchronizer, ClusterNameService clusterNameService) {
        this.coordinator = coordinator;
        this.transactionMetadataManager = transactionMetadataManager;
        this.producerIdManager = producerIdManager;
        this.transactionSynchronizer = transactionSynchronizer;
        this.clusterNameService = clusterNameService;
    }

    public TransactionMetadata initProducer(String clientId, String transactionId, int transactionTimeout) {
        // 幂等
        if (StringUtils.isBlank(transactionId)) {
            transactionId = clientId + IDEMPOTENCE_TRANSACTION_ID_SUFFIX;
        } else {
            // 幂等不判断协调者
            checkCoordinatorState(clientId, transactionId);
        }

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null) {
            transactionMetadata = transactionMetadataManager.getOrCreateTransaction(new TransactionMetadata(transactionId, clientId,
                    producerIdManager.generateId(), transactionTimeout, SystemClock.now()));
        }

        synchronized (transactionMetadata) {
            return doInitProducer(transactionMetadata, transactionTimeout);
        }
    }

    protected TransactionMetadata doInitProducer(TransactionMetadata transactionMetadata, int transactionTimeout) {
        transactionMetadata.clear();
        transactionMetadata.nextProducerEpoch();
        transactionMetadata.nextEpoch();
        transactionMetadata.setTimeout(transactionTimeout);
        transactionMetadata.updateLastTime();
        transactionMetadata.transitionStateTo(TransactionState.EMPTY);
        return transactionMetadata;
    }

    public Map<String, List<PartitionMetadataAndError>> addPartitionsToTxn(String clientId, String transactionId, long producerId, short producerEpoch, Map<String, List<Integer>> partitions) {
        checkCoordinatorState(clientId, transactionId);

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
        }
        synchronized (transactionMetadata) {
            if (transactionMetadata.getProducerId() != producerId || !StringUtils.equals(transactionMetadata.getApp(), clientId)) {
                throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
            }
            if (transactionMetadata.getProducerEpoch() != producerEpoch) {
                throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
            }
            if (transactionMetadata.isExpired()) {
                throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
            }
            if (transactionMetadata.isPrepared()) {
                throw new TransactionException(KafkaErrorCode.CONCURRENT_TRANSACTIONS.getCode());
            }
            return doAddPartitionsToTxn(transactionMetadata, partitions);
        }
    }

    protected Map<String, List<PartitionMetadataAndError>> doAddPartitionsToTxn(TransactionMetadata transactionMetadata, Map<String, List<Integer>> partitions) {
        transactionMetadata.transitionStateTo(TransactionState.ONGOING);
        transactionMetadata.updateLastTime();

        Set<TransactionPrepare> prepareSet = Sets.newHashSet();

        Map<String, List<PartitionMetadataAndError>> result = Maps.newHashMapWithExpectedSize(partitions.size());
        for (Map.Entry<String, List<Integer>> entry : partitions.entrySet()) {
            List<PartitionMetadataAndError> partitionMetadataAndErrors = Lists.newArrayListWithCapacity(entry.getValue().size());
            result.put(entry.getKey(), partitionMetadataAndErrors);

            TopicName topic = TopicName.parse(entry.getKey());
            TopicConfig topicConfig = clusterNameService.getTopicConfig(topic);

            for (Integer partition : entry.getValue()) {
                PartitionGroup partitionGroup = null;
                if (topicConfig != null) {
                    partitionGroup = topicConfig.fetchPartitionGroupByPartition((short) partition.intValue());
                }
                if (partitionGroup == null) {
                    partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, KafkaErrorCode.UNKNOWN_TOPIC_OR_PARTITION.getCode()));
                } else if (partitionGroup.getLeader() == null || partitionGroup.getLeader() <= 0) {
                    partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, KafkaErrorCode.NOT_LEADER_FOR_PARTITION.getCode()));
                } else {
                    Broker broker = clusterNameService.getNameService().getBroker(partitionGroup.getLeader());
                    if (broker == null) {
                        partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, KafkaErrorCode.NOT_LEADER_FOR_PARTITION.getCode()));
                    } else {
                        TransactionPrepare prepare = new TransactionPrepare(topic.getFullName(), (short) partition.intValue(),
                                transactionMetadata.getApp(), broker.getId(), broker.getIp(), broker.getPort(),
                                transactionMetadata.getId(), transactionMetadata.getProducerId(), transactionMetadata.getProducerEpoch(),
                                transactionMetadata.getEpoch(), transactionMetadata.getTimeout(), SystemClock.now());
                        prepareSet.add(prepare);
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(prepareSet)) {
            try {
                transactionSynchronizer.prepare(transactionMetadata, prepareSet);
                transactionMetadata.addPrepare(prepareSet);

                for (TransactionPrepare transactionPrepare : prepareSet) {
                    result.get(transactionPrepare.getTopic()).add(new PartitionMetadataAndError(transactionPrepare.getPartition(), KafkaErrorCode.NONE.getCode()));
                }
            } catch (Exception e) {
                logger.error("transaction prepare exception, metadata:{}, prepare: {}", transactionMetadata, prepareSet, e);
                for (TransactionPrepare transactionPrepare : prepareSet) {
                    result.get(transactionPrepare.getTopic()).add(new PartitionMetadataAndError(transactionPrepare.getPartition(), KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode()));
                }
            }
        }

        return result;
    }

    public boolean endTxn(String clientId, String transactionId, long producerId, short producerEpoch, boolean isCommit) {
        checkCoordinatorState(clientId, transactionId);

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
        }
        synchronized (transactionMetadata) {
            if (transactionMetadata.getProducerId() != producerId || !StringUtils.equals(transactionMetadata.getApp(), clientId)) {
                throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
            }
            if (transactionMetadata.getProducerEpoch() != producerEpoch) {
                throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
            }
            if (transactionMetadata.isExpired()) {
                throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
            }
            if (transactionMetadata.isCompleted()) {
                throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
            }
            return doEndTxn(transactionMetadata, isCommit);
        }
    }

    protected boolean doEndTxn(TransactionMetadata transactionMetadata, boolean isCommit) {
        try {
            if (isCommit) {
                doCommit(transactionMetadata);
            } else {
                doAbort(transactionMetadata);
            }

            transactionMetadata.clear();
            transactionMetadata.nextEpoch();
            return true;
        } catch (Exception e) {
            logger.error("endTxn exception, metadata: {}, isCommit: {}", transactionMetadata, isCommit, e);
            throw new TransactionException(e, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
    }

    protected void doCommit(TransactionMetadata transactionMetadata) throws Exception {
        if (!transactionMetadata.getState().equals(TransactionState.PREPARE_COMMIT)) {
            if (!transactionSynchronizer.prepareCommit(transactionMetadata, transactionMetadata.getPrepare())) {
                throw new JoyQueueException(String.format("prepare commit transaction failed, metadata: %s", transactionMetadata), JoyQueueCode.CN_UNKNOWN_ERROR.getCode());
            }
            transactionMetadata.transitionStateTo(TransactionState.PREPARE_COMMIT);
        }

        if (!transactionSynchronizer.commit(transactionMetadata, transactionMetadata.getPrepare(), transactionMetadata.getOffsets())) {
            throw new JoyQueueException(String.format("commit transaction failed, metadata: %s", transactionMetadata), JoyQueueCode.CN_UNKNOWN_ERROR.getCode());
        }
        transactionMetadata.transitionStateTo(TransactionState.COMPLETE_COMMIT);
    }

    protected void doAbort(TransactionMetadata transactionMetadata) throws Exception {
        if (!transactionMetadata.getState().equals(TransactionState.PREPARE_ABORT)) {
            if (!transactionSynchronizer.prepareAbort(transactionMetadata, transactionMetadata.getPrepare())) {
                throw new JoyQueueException(String.format("prepare abort transaction failed, metadata: %s", transactionMetadata), JoyQueueCode.CN_UNKNOWN_ERROR.getCode());
            }
            transactionMetadata.transitionStateTo(TransactionState.PREPARE_ABORT);
        }

        if (!transactionSynchronizer.abort(transactionMetadata, transactionMetadata.getPrepare())) {
            throw new JoyQueueException(String.format("abort transaction failed, metadata: %s", transactionMetadata), JoyQueueCode.CN_UNKNOWN_ERROR.getCode());
        }
        transactionMetadata.transitionStateTo(TransactionState.COMPLETE_ABORT);
    }

    protected void checkCoordinatorState(String clientId, String transactionId) {
        if (!isStarted()) {
            throw new TransactionException(KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
        if (!coordinator.isCurrentTransaction(clientId)) {
            throw new TransactionException(KafkaErrorCode.NOT_COORDINATOR.getCode());
        }
    }
}