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
package org.joyqueue.broker.kafka;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import org.joyqueue.broker.kafka.coordinator.transaction.ProducerSequenceManager;
import org.joyqueue.broker.kafka.coordinator.transaction.TransactionCoordinator;
import org.joyqueue.broker.kafka.coordinator.transaction.TransactionIdManager;

/**
 * KafkaContext
 *
 * author: gaohaoxiang
 * date: 2018/11/7
 */
public class KafkaContext {

    private KafkaConfig config;
    private GroupCoordinator groupCoordinator;
    private TransactionCoordinator transactionCoordinator;
    private TransactionIdManager transactionIdManager;
    private ProducerSequenceManager producerSequenceManager;
    private BrokerContext brokerContext;

    public KafkaContext(KafkaConfig config, GroupCoordinator groupCoordinator, TransactionCoordinator transactionCoordinator, TransactionIdManager transactionIdManager,
                        ProducerSequenceManager producerSequenceManager, BrokerContext brokerContext) {
        this.config = config;
        this.groupCoordinator = groupCoordinator;
        this.transactionCoordinator = transactionCoordinator;
        this.transactionIdManager = transactionIdManager;
        this.producerSequenceManager = producerSequenceManager;
        this.brokerContext = brokerContext;
    }

    public KafkaConfig getConfig() {
        return config;
    }

    public GroupCoordinator getGroupCoordinator() {
        return groupCoordinator;
    }

    public TransactionCoordinator getTransactionCoordinator() {
        return transactionCoordinator;
    }

    public TransactionIdManager getTransactionIdManager() {
        return transactionIdManager;
    }

    public ProducerSequenceManager getProducerSequenceManager() {
        return producerSequenceManager;
    }

    public BrokerContext getBrokerContext() {
        return brokerContext;
    }
}