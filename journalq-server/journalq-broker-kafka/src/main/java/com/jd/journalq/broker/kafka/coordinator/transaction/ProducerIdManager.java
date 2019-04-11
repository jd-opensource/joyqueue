package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.jd.journalq.toolkit.service.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ProducerIdManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/11
 */
public class ProducerIdManager extends Service {

    private final AtomicLong producerIdSeq = new AtomicLong();

    public long generateId() {
        return producerIdSeq.incrementAndGet();
    }
}