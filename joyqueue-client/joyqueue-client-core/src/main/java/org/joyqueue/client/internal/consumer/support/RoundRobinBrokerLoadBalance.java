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

import org.joyqueue.client.internal.consumer.BrokerLoadBalance;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignment;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignments;
import com.jd.laf.extension.Extension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RoundRobinBrokerLoadBalance
 *
 * author: gaohaoxiang
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