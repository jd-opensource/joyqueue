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

public class PartitionGroupPendingStat implements PendingStat<Short,Long> {
    private String topic;
    private String app;
    private int  partitionGroup;
    private long pending;
    private Map<Short/*partition*/,Long/*pending*/> partitionPendingStatMap =new HashMap<>();
    private Map<Short/*partition*/,PartitionStat> partitionStatHashMap =new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    @Override
    public void setPendingStatSubMap(Map<Short, Long> subMap) {
        this.partitionPendingStatMap=subMap;
    }

    @Override
    public Map<Short, Long> getPendingStatSubMap() {
        return partitionPendingStatMap;
    }

    public Map<Short, PartitionStat> getPartitionStatHashMap() {
        return partitionStatHashMap;
    }

    public void setPartitionStatHashMap(Map<Short, PartitionStat> partitionStatHashMap) {
        this.partitionStatHashMap = partitionStatHashMap;
    }
}
