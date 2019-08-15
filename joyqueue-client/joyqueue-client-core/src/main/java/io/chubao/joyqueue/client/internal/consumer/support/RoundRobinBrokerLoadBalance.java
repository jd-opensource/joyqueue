package io.chubao.joyqueue.client.internal.consumer.support;

import io.chubao.joyqueue.client.internal.consumer.BrokerLoadBalance;
import io.chubao.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignment;
import io.chubao.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignments;
import com.jd.laf.extension.Extension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RoundRobinBrokerLoadBalance
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/11
 */
@Extension(singleton = false)
public class RoundRobinBrokerLoadBalance implements BrokerLoadBalance {

    public static final String NAME = "roundrobin";

    private AtomicInteger next = new AtomicInteger();

    @Override
    public BrokerAssignment loadBalance(BrokerAssignments brokerAssignments) {
        List<BrokerAssignment> assignments = brokerAssignments.getAssignments();
        int index = next.getAndIncrement();
        if (index >= assignments.size()) {
            next.set(1);
            index = 0;
        }
        return assignments.get(index);
    }

    @Override
    public String type() {
        return NAME;
    }
}