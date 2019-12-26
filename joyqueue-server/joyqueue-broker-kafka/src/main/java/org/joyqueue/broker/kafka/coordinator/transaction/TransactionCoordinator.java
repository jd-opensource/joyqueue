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

import org.joyqueue.broker.kafka.coordinator.Coordinator;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import org.joyqueue.broker.kafka.model.OffsetAndMetadata;
import org.joyqueue.broker.kafka.model.PartitionMetadataAndError;
import org.joyqueue.domain.Broker;
import org.joyqueue.toolkit.service.Service;

import java.util.List;
import java.util.Map;

/**
 * TransactionCoordinator
 *
 * author: gaohaoxiang
 * date: 2019/4/10
 */
public class TransactionCoordinator extends Service {

    private Coordinator coordinator;
    private TransactionMetadataManager transactionMetadataManager;
    private TransactionHandler transactionHandler;
    private TransactionOffsetHandler transactionOffsetHandler;

    public TransactionCoordinator(Coordinator coordinator, TransactionMetadataManager transactionMetadataManager,
                                  TransactionHandler transactionHandler, TransactionOffsetHandler transactionOffsetHandler) {
        this.coordinator = coordinator;
        this.transactionMetadataManager = transactionMetadataManager;
        this.transactionHandler = transactionHandler;
        this.transactionOffsetHandler = transactionOffsetHandler;
    }

    public Broker findCoordinator(String transactionId) {
        return coordinator.findTransaction(transactionId);
    }

    public boolean isCurrentCoordinator(String transactionId) {
        return coordinator.isCurrentTransaction(transactionId);
    }

    public TransactionMetadata handleInitProducer(String clientId, String transactionId, int transactionTimeout) {
        return transactionHandler.initProducer(clientId, transactionId, transactionTimeout);
    }

    public Map<String, List<PartitionMetadataAndError>> handleAddPartitionsToTxn(String clientId, String transactionId, long producerId, short producerEpoch, Map<String, List<Integer>> partitions) {
        return transactionHandler.addPartitionsToTxn(clientId, transactionId, producerId, producerEpoch, partitions);
    }

    public boolean handleEndTxn(String clientId, String transactionId, long producerId, short producerEpoch, boolean isCommit) {
        return transactionHandler.endTxn(clientId, transactionId, producerId, producerEpoch, isCommit);
    }

    public boolean handleAddOffsetsToTxn(String clientId, String transactionId, String groupId, long producerId, short producerEpoch) {
        return transactionOffsetHandler.addOffsetsToTxn(clientId, transactionId, groupId, producerId, producerEpoch);
    }

    public Map<String, List<PartitionMetadataAndError>> handleCommitOffset(String clientId, String transactionId, String groupId,
                                                                           long producerId, short producerEpoch, Map<String, List<OffsetAndMetadata>> offsetAndMetadata) {
        return transactionOffsetHandler.commitOffset(clientId, transactionId, groupId, producerId, producerEpoch, offsetAndMetadata);
    }

    public TransactionMetadata getTransaction(String transactionId) {
        return transactionMetadataManager.getTransaction(transactionId);
    }

    public boolean removeTransaction(TransactionMetadata transaction) {
        return transactionMetadataManager.removeTransaction(transaction.getId());
    }

    public boolean removeTransaction(String transactionId) {
        return transactionMetadataManager.removeTransaction(transactionId);
    }
}