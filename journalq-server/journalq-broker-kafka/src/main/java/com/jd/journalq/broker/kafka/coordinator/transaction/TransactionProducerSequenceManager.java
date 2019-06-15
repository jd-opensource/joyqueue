package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jd.journalq.broker.kafka.config.KafkaConfig;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TransactionProducerSequenceManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/14
 */
public class TransactionProducerSequenceManager {

    private KafkaConfig config;
    private Cache<String, AtomicLong> producerIdCache;

    public TransactionProducerSequenceManager(KafkaConfig config) {
        this.config = config;
        this.producerIdCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getTransactionProducerSequenceExpire(), TimeUnit.MILLISECONDS)
                .build();
    }

    public long getSequence(long producerId, short producerEpoch) {
        try {
            return producerIdCache.get(producerId + "_" + producerEpoch, new Callable<AtomicLong>() {
                @Override
                public AtomicLong call() throws Exception {
                    return new AtomicLong();
                }
            }).get();
        } catch (ExecutionException e) {
            return 0;
        }
    }

    public void updateSequence(long producerId, short producerEpoch, long sequence) {
        try {
            producerIdCache.get(producerId + "_" + producerEpoch, new Callable<AtomicLong>() {
                @Override
                public AtomicLong call() throws Exception {
                    return new AtomicLong();
                }
            }).set(sequence);
        } catch (ExecutionException e) {
        }
    }
}