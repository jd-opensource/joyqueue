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
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.coordinator.Coordinator;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionOffset;
import org.joyqueue.broker.kafka.coordinator.transaction.exception.TransactionException;
import org.joyqueue.broker.kafka.coordinator.transaction.synchronizer.TransactionSynchronizer;
import org.joyqueue.broker.kafka.model.OffsetAndMetadata;
import org.joyqueue.broker.kafka.model.PartitionMetadataAndError;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TransactionOffsetHandler
 *
 * author: gaohaoxiang
 * date: 2019/4/16
 */
public class TransactionOffsetHandler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionOffsetHandler.class);

    private Coordinator coordinator;
    private TransactionMetadataManager transactionMetadataManager;
    private TransactionSynchronizer transactionSynchronizer;

    public TransactionOffsetHandler(Coordinator coordinator, TransactionMetadataManager transactionMetadataManager, TransactionSynchronizer transactionSynchronizer) {
        this.coordinator = coordinator;
        this.transactionMetadataManager = transactionMetadataManager;
        this.transactionSynchronizer = transactionSynchronizer;
    }

    public boolean addOffsetsToTxn(String clientId, String transactionId, String groupId, long producerId, short producerEpoch) {
        checkCoordinatorState(clientId, transactionId);

        TransactionMetadata transactionMetadata = transactionMetadataManager.getTransaction(transactionId);
        if (transactionMetadata == null || transactionMetadata.getProducerId() != producerId || !StringUtils.equals(transactionMetadata.getApp(), clientId)) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_ID_MAPPING.getCode());
        }
        if (transactionMetadata.getProducerEpoch() != producerEpoch) {
            throw new TransactionException(KafkaErrorCode.INVALID_PRODUCER_EPOCH.getCode());
        }
        if (transactionMetadata.isPrepared()) {
            throw new TransactionException(KafkaErrorCode.CONCURRENT_TRANSACTIONS.getCode());
        }

        transactionMetadata.updateLastTime();
        return true;
    }

    public Map<String, List<PartitionMetadataAndError>> commitOffset(String clientId, String transactionId, String groupId,
                                                                     long producerId, short producerEpoch, Map<String, List<OffsetAndMetadata>> offsetts) {
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
            if (transactionMetadata.isPrepared()) {
                throw new TransactionException(KafkaErrorCode.CONCURRENT_TRANSACTIONS.getCode());
            }
            return doCommitOffset(transactionMetadata, offsetts);
        }
    }

    protected Map<String, List<PartitionMetadataAndError>> doCommitOffset(TransactionMetadata transactionMetadata, Map<String, List<OffsetAndMetadata>> offsets) {
        transactionMetadata.updateLastTime();

        Set<TransactionOffset> transactionOffsets = Sets.newHashSet();

        for (Map.Entry<String, List<OffsetAndMetadata>> entry : offsets.entrySet()) {
            for (OffsetAndMetadata offsetAndMetadata : entry.getValue()) {
                transactionOffsets.add(new TransactionOffset(entry.getKey(), (short) offsetAndMetadata.getPartition(),
                        offsetAndMetadata.getOffset(), transactionMetadata.getApp(), transactionMetadata.getId(),
                        transactionMetadata.getProducerId(), transactionMetadata.getProducerEpoch(), transactionMetadata.getEpoch(),
                        transactionMetadata.getTimeout(), SystemClock.now()));
            }
        }

        try {
            if (CollectionUtils.isNotEmpty(transactionOffsets)) {
                transactionMetadata.addOffsets(transactionOffsets);
                transactionSynchronizer.commitOffset(transactionMetadata, transactionOffsets);
            }
            return buildPartitionMetadataAndError(offsets, KafkaErrorCode.NONE.getCode());
        } catch (Exception e) {
            logger.error("commitOffset exception, metadata: {}, offsets: {}", transactionMetadata, offsets, e);
            throw new TransactionException(e, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
    }

    protected Map<String, List<PartitionMetadataAndError>> buildPartitionMetadataAndError(Map<String, List<OffsetAndMetadata>> offsets, short code) {
        Map<String, List<PartitionMetadataAndError>> result = Maps.newHashMapWithExpectedSize(offsets.size());
        for (Map.Entry<String, List<OffsetAndMetadata>> entry : offsets.entrySet()) {
            List<PartitionMetadataAndError> partitionMetadataAndErrors = Lists.newArrayListWithCapacity(entry.getValue().size());
            for (OffsetAndMetadata offsetAndMetadata : entry.getValue()) {
                partitionMetadataAndErrors.add(new PartitionMetadataAndError(offsetAndMetadata.getPartition(), code));
            }
            result.put(entry.getKey(), partitionMetadataAndErrors);
        }
        return result;
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