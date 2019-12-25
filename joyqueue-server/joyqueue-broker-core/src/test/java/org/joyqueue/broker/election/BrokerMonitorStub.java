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
package org.joyqueue.broker.election;

import org.joyqueue.broker.monitor.BrokerMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuduohui on 2018/12/6.
 */
public class BrokerMonitorStub extends BrokerMonitor {
    private static Logger logger = LoggerFactory.getLogger(BrokerMonitorStub.class);

    @Override
    public void onReplicateMessage(String topic, int partitionGroup, long count, long size, double time) {
        logger.debug("Monitor replicate message of topic {} partition group {}, " +
                "count is {}, size is {}, time is {}",
                topic, partitionGroup, count, size, time);
    }

    @Override
    public void onAppendReplicateMessage(String topic, int partitionGroup, long count, long size, double time) {
        logger.debug("Monitor append replicate message of topic {} partition group {}, " +
                "count is {}, size is {}, time is {}",
                topic, partitionGroup, count, size, time);
    }
}
