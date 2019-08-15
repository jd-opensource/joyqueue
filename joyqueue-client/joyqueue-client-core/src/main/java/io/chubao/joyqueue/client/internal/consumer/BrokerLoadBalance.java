package io.chubao.joyqueue.client.internal.consumer;

import io.chubao.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignment;
import io.chubao.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignments;
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