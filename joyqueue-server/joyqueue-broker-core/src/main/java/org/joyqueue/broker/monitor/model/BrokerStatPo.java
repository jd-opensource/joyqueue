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
package org.joyqueue.broker.monitor.model;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * brokerstat
 *
 * author: gaohaoxiang
 * date: 2018/10/12
 */
public class BrokerStatPo extends BasePo {

    private Integer brokerId;
    private int version;
    private DeQueueStatPo deQueueStat;
    private EnQueueStatPo enQueueStat;
    private Map<String, TopicStatPo> topicStatMap = Maps.newHashMap();
    private ReplicationStatPo replicationStat;

    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public DeQueueStatPo getDeQueueStat() {
        return deQueueStat;
    }

    public void setDeQueueStat(DeQueueStatPo deQueueStat) {
        this.deQueueStat = deQueueStat;
    }

    public EnQueueStatPo getEnQueueStat() {
        return enQueueStat;
    }

    public void setEnQueueStat(EnQueueStatPo enQueueStat) {
        this.enQueueStat = enQueueStat;
    }

    public Map<String, TopicStatPo> getTopicStatMap() {
        return topicStatMap;
    }

    public void setTopicStatMap(Map<String, TopicStatPo> topicStatMap) {
        this.topicStatMap = topicStatMap;
    }

    public void setReplicationStat(ReplicationStatPo replicationStat) {
        this.replicationStat = replicationStat;
    }

    public ReplicationStatPo getReplicationStat() {
        return replicationStat;
    }
}