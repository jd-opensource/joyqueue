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
package org.joyqueue.client.internal.consumer.support;

import com.jd.laf.extension.Extension;
import org.joyqueue.client.internal.consumer.BrokerLoadBalance;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignment;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignments;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BinThreadBrokerLoadBalance
 *
 * author: gaohaoxiang
 * date: 2020/11/3
 */
@Extension(singleton = false)
public class BindThreadBrokerLoadBalance implements BrokerLoadBalance {

    public static final String NAME = "bind-thread";

    private RoundRobinBrokerLoadBalance roundRobinBrokerLoadBalance = new RoundRobinBrokerLoadBalance();

    private AtomicInteger next = new AtomicInteger();
    private ThreadLocal<Integer> threadLocal = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return next.getAndIncrement();
        }
    };

    @Override
    public BrokerAssignment loadBalance(BrokerAssignments brokerAssignments) {
        List<BrokerAssignment> assignments = brokerAssignments.getAssignments();
        Integer index = threadLocal.get();
        if (next.get() < assignments.size()) {
            return roundRobinBrokerLoadBalance.loadBalance(brokerAssignments);
        }
        return assignments.get(index % assignments.size());
    }

    @Override
    public String type() {
        return NAME;
    }
}