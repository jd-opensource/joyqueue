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

import com.google.common.collect.Maps;
import org.joyqueue.client.internal.consumer.BrokerLoadBalance;
import com.google.common.base.Preconditions;
import org.joyqueue.client.internal.Plugins;

import java.util.concurrent.ConcurrentMap;

/**
 * BrokerLoadBalanceManager
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class BrokerLoadBalanceManager {

    private ConcurrentMap<String, BrokerLoadBalance> brokerLoadBalanceMap = Maps.newConcurrentMap();

    public BrokerLoadBalance getBrokerLoadBalance(String topic, String loadBalanceType) {
        BrokerLoadBalance brokerLoadBalance = brokerLoadBalanceMap.get(topic);
        if (brokerLoadBalance == null) {
            brokerLoadBalance = create(loadBalanceType);
            BrokerLoadBalance oldBrokerLoadBalance = brokerLoadBalanceMap.putIfAbsent(topic, brokerLoadBalance);
            if (oldBrokerLoadBalance != null) {
                brokerLoadBalance = oldBrokerLoadBalance;
            }
        }
        return brokerLoadBalance;
    }


    private BrokerLoadBalance create(String loadBalanceType) {
        BrokerLoadBalance loadBalance = Plugins.LOADBALANCE.get(loadBalanceType);
        Preconditions.checkArgument(loadBalance != null, String.format("no balance found. type: %s", loadBalanceType));
        return loadBalance;
    }
}