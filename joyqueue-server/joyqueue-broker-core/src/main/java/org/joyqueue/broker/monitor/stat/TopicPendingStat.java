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
package org.joyqueue.broker.monitor.stat;

import org.joyqueue.broker.monitor.PendingStat;

import java.util.HashMap;
import java.util.Map;

public class TopicPendingStat implements PendingStat<String,ConsumerPendingStat> {
    private String topic;
    private long  pending;
    private Map<String/*app*/,ConsumerPendingStat> consumerPendingStatMap=new HashMap<>();
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }



    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }


    @Override
    public void setPendingStatSubMap(Map<String, ConsumerPendingStat> subMap) {
        this.consumerPendingStatMap=subMap;
    }

    @Override
    public Map<String, ConsumerPendingStat> getPendingStatSubMap() {
        return consumerPendingStatMap;
    }
}
