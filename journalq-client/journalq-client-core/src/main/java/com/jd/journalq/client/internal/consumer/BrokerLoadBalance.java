package com.jd.journalq.client.internal.consumer;

import com.jd.journalq.client.internal.consumer.coordinator.domain.BrokerAssignment;
import com.jd.journalq.client.internal.consumer.coordinator.domain.BrokerAssignments;
import com.jd.laf.extension.Type;

/**
 * BrokerLoadBalance
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/11
 */
public interface BrokerLoadBalance extends Type<String> {

    BrokerAssignment loadBalance(BrokerAssignments brokerAssignments);
}