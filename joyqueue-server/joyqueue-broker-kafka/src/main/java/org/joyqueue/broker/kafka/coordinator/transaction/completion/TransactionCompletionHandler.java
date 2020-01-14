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
package org.joyqueue.broker.kafka.coordinator.transaction.completion;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.Coordinator;
import org.joyqueue.broker.kafka.coordinator.transaction.TransactionMetadataManager;
import org.joyqueue.broker.kafka.coordinator.transaction.log.TransactionLog;
import org.joyqueue.broker.kafka.coordinator.transaction.log.TransactionLogSegment;
import org.joyqueue.broker.kafka.coordinator.transaction.synchronizer.TransactionSynchronizer;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TransactionCompletionHandler
 *
 * author: gaohaoxiang
 * date: 2019/4/19
 */
public class TransactionCompletionHandler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCompletionHandler.class);

    private KafkaConfig config;
    private Coordinator coordinator;
    private TransactionMetadataManager transactionMetadataManager;
    private TransactionLog transactionLog;
    private TransactionSynchronizer transactionSynchronizer;

    private Map<Short, TransactionSegmentCompletionHandler> handlerMap = Maps.newHashMap();

    public TransactionCompletionHandler(KafkaConfig config, Coordinator coordinator, TransactionMetadataManager transactionMetadataManager,
                                        TransactionLog transactionLog, TransactionSynchronizer transactionSynchronizer) {
        this.config = config;
        this.coordinator = coordinator;
        this.transactionMetadataManager = transactionMetadataManager;
        this.transactionLog = transactionLog;
        this.transactionSynchronizer = transactionSynchronizer;
    }

    public void handle() {
        try {
            Set<Short> partitions = Sets.newHashSet(transactionLog.getPartitions());

            Iterator<Map.Entry<Short, TransactionSegmentCompletionHandler>> handlerIterator = handlerMap.entrySet().iterator();
            while (handlerIterator.hasNext()) {
                Map.Entry<Short, TransactionSegmentCompletionHandler> entry = handlerIterator.next();
                if (!partitions.contains(entry.getKey())) {
                    handlerIterator.remove();
                    transactionLog.removeSegment(entry.getKey());
                }
            }

            for (Short partition : partitions) {
                if (handlerMap.containsKey(partition)) {
                    continue;
                }
                TransactionLogSegment transactionLogSegment = transactionLog.getSegment(partition);
                if (transactionLogSegment == null) {
                    continue;
                }
                handlerMap.put(partition, new TransactionSegmentCompletionHandler(config, coordinator, transactionMetadataManager, transactionLogSegment, transactionSynchronizer));
            }

            for (Map.Entry<Short, TransactionSegmentCompletionHandler> entry : handlerMap.entrySet()) {
                entry.getValue().handle();
            }
        } catch (Exception e) {
            logger.error("transaction compensate exception", e);
        }
    }
}