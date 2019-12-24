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
package org.joyqueue.broker.kafka.coordinator.transaction.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import org.joyqueue.domain.Broker;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TransactionHelper
 *
 * author: gaohaoxiang
 * date: 2019/4/18
 */
public class TransactionHelper {

    public static Map<Broker, List<TransactionPrepare>> splitPrepareByBroker(Set<TransactionPrepare> prepareList) {
        if (CollectionUtils.isEmpty(prepareList)) {
            return Collections.emptyMap();
        }
        Map<Broker, List<TransactionPrepare>> result = Maps.newHashMapWithExpectedSize(prepareList.size());
        for (TransactionPrepare prepare : prepareList) {
            Broker broker = new Broker();
            broker.setId(prepare.getBrokerId());
            broker.setIp(prepare.getBrokerHost());
            broker.setPort(prepare.getBrokerPort());

            List<TransactionPrepare> brokerPrepareList = result.get(broker);
            if (brokerPrepareList == null) {
                brokerPrepareList = Lists.newLinkedList();
                result.put(broker, brokerPrepareList);
            }

            brokerPrepareList.add(prepare);
        }
        return result;
    }
}