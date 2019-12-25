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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.joyqueue.broker.kafka.config.KafkaConfig;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProducerSequenceManager
 *
 * author: gaohaoxiang
 * date: 2019/6/14
 */
public class ProducerSequenceManager {

    private KafkaConfig config;
    private Cache<String, AtomicLong> producerIdCache;

    public ProducerSequenceManager(KafkaConfig config) {
        this.config = config;
        this.producerIdCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getTransactionProducerSequenceExpire(), TimeUnit.MILLISECONDS)
                .build();
    }

    public boolean checkSequence(String app, long producerId, short producerEpoch, int partition, long sequence) {
        long lastSequence = getSequence(app, producerId, producerEpoch, partition);
        if (lastSequence <= 0 || sequence == 0 || sequence == lastSequence + 1) {
            return true;
        }
        return false;
    }

    // 因为没有持久化producerId，所以可能导致协调者变化后producerId变化导致重复producerId，所以用app再次区分
    public long getSequence(String app, long producerId, short producerEpoch, int partition) {
        return doGetSequence(app, producerId, producerEpoch, partition).get();
    }

    public void updateSequence(String app, long producerId, short producerEpoch, int partition, long sequence) {
        doGetSequence(app, producerId, producerEpoch, partition).set(sequence);
    }

    protected AtomicLong doGetSequence(String app, long producerId, short producerEpoch, int partition) {
        try {
            return producerIdCache.get(generateKey(app, producerId, producerEpoch, partition), new Callable<AtomicLong>() {
                @Override
                public AtomicLong call() throws Exception {
                    return new AtomicLong();
                }
            });
        } catch (ExecutionException e) {
            return new AtomicLong();
        }
    }

    protected String generateKey(String app, long producerId, short producerEpoch, int partition) {
        return String.format("%s_%s_%s_%s", app, producerId, producerEpoch, partition);
    }
}