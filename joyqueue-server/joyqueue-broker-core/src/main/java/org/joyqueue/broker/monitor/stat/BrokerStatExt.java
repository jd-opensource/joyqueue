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

import java.io.Serializable;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

/**
 * 对BrokerStat 进行扩展，包含 如topic app, partition ,partitionGroup 维度的 consume pending监控信息
 **/
public class BrokerStatExt implements Serializable {

    private transient BrokerStat brokerStat;
    private Integer brokerId;
    private Map<String/*topic*/, TopicPendingStat> topicPendingStatMap = new HashMap<>();
    private MemoryUsage heap;
    private MemoryUsage nonHeap;
    private long timeStamp;

    private long archiveConsumePending;
    private long archiveProducePending;
    private Map<String,Long> topicArchiveProducePending;

    public BrokerStatExt(BrokerStat brokerStat) {
        setBrokerStat(brokerStat);
    }

    public BrokerStat getBrokerStat() {
        return brokerStat;
    }

    public void setBrokerStat(BrokerStat brokerStat) {
        this.brokerStat = brokerStat;
        if (brokerStat != null) {
            brokerId = brokerStat.getBrokerId();
        }
    }

    public Map<String, TopicPendingStat> getTopicPendingStatMap() {
        return topicPendingStatMap;
    }

    public void setTopicPendingStatMap(Map<String, TopicPendingStat> topicPendingStatMap) {
        this.topicPendingStatMap = topicPendingStatMap;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public MemoryUsage getHeap() {
        return heap;
    }

    public void setHeap(MemoryUsage heap) {
        this.heap = heap;
    }

    public MemoryUsage getNonHeap() {
        return nonHeap;
    }

    public void setNonHeap(MemoryUsage nonHeap) {
        this.nonHeap = nonHeap;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getArchiveConsumePending() {
        return archiveConsumePending;
    }

    public void setArchiveConsumePending(long archiveConsumePending) {
        this.archiveConsumePending = archiveConsumePending;
    }

    public long getArchiveProducePending() {
        return archiveProducePending;
    }

    public void setArchiveProducePending(long archiveProducePending) {
        this.archiveProducePending = archiveProducePending;
    }

    public Map<String, Long> getTopicArchiveProducePending() {
        return topicArchiveProducePending;
    }

    public void setTopicArchiveProducePending(Map<String, Long> topicArchiveProducePending) {
        this.topicArchiveProducePending = topicArchiveProducePending;
    }
}
