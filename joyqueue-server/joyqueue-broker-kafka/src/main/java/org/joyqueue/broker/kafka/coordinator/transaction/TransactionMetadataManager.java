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

import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.group.GroupMetadataManager;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TransactionMetadataManager
 *
 * author: gaohaoxiang
 * date: 2019/4/10
 */
public class TransactionMetadataManager {

    protected static final Logger logger = LoggerFactory.getLogger(GroupMetadataManager.class);

    private KafkaConfig config;
    private org.joyqueue.broker.coordinator.transaction.TransactionMetadataManager transactionMetadataManager;

    public TransactionMetadataManager(KafkaConfig config, org.joyqueue.broker.coordinator.transaction.TransactionMetadataManager transactionMetadataManager) {
        this.config = config;
        this.transactionMetadataManager = transactionMetadataManager;
    }

    public TransactionMetadata tryGetTransaction(String transactionId) {
        return transactionMetadataManager.tryGetTransaction(transactionId);
    }

    public TransactionMetadata getTransaction(String transactionId) {
        return transactionMetadataManager.getTransaction(transactionId);
    }

    public TransactionMetadata getOrCreateTransaction(TransactionMetadata transaction) {
        return transactionMetadataManager.getOrCreateTransaction(transaction);
    }

    public List<TransactionMetadata> getTransactions() {
        return transactionMetadataManager.getTransactions();
    }

    public boolean removeTransaction(TransactionMetadata transaction) {
        return transactionMetadataManager.removeTransaction(transaction.getId());
    }

    public boolean removeTransaction(String transactionId) {
        return transactionMetadataManager.removeTransaction(transactionId);
    }

}