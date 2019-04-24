package com.jd.journalq.broker.kafka.coordinator.transaction;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ProducerIdManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/11
 */
public class ProducerIdManager {

    private final AtomicLong producerIdSeq = new AtomicLong();

    public long generateId() {
        return producerIdSeq.getAndIncrement();
    }
}