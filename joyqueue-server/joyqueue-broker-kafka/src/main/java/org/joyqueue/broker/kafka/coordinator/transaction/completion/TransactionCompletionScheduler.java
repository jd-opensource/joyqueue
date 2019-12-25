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

import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TransactionCompletionScheduler
 *
 * author: gaohaoxiang
 * date: 2019/4/15
 */
public class TransactionCompletionScheduler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCompletionScheduler.class);

    private KafkaConfig config;
    private TransactionCompletionHandler transactionCompletionHandler;

    private ScheduledExecutorService executor;

    public TransactionCompletionScheduler(KafkaConfig config, TransactionCompletionHandler transactionCompletionHandler) {
        this.config = config;
        this.transactionCompletionHandler = transactionCompletionHandler;
    }

    @Override
    protected void validate() throws Exception {
        executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("joyqueue-transaction-compensate"));
    }

    @Override
    protected void doStart() throws Exception {
        executor.scheduleAtFixedRate(new TransactionCompletionThread(transactionCompletionHandler),
                config.getTransactionLogInterval(), config.getTransactionLogInterval(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
