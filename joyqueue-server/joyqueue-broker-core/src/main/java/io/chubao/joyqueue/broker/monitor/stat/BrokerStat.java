/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.broker.monitor.stat;


import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.monitor.BrokerMonitorConsts;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * BrokerStat
 *
 * author: gaohaoxiang
 * date: 2018/11/16
 */
public class BrokerStat implements Serializable {

    private static final long serialVersionUID = -4637157513730009816L;

    public static final int VERSION = BrokerMonitorConsts.STAT_VERSION;

    private Integer brokerId;
    private ConnectionStat connectionStat = new ConnectionStat();
    private EnQueueStat enQueueStat = new EnQueueStat();
    private DeQueueStat deQueueStat = new DeQueueStat();
    private ConcurrentMap<String /** topic **/, TopicStat> topicStatMap = Maps.newConcurrentMap();
    private ReplicationStat replicationStat = new ReplicationStat();

    public BrokerStat(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public TopicStat getOrCreateTopicStat(String topic) {
        TopicStat topicStat = topicStatMap.get(topic);
        if (topicStat == null) {
            topicStatMap.putIfAbsent(topic, new TopicStat(topic));
            topicStat = topicStatMap.get(topic);
        }
        return topicStat;
    }

    public ConnectionStat getConnectionStat() {
        return this.connectionStat;
    }

    public DeQueueStat getDeQueueStat() {
        return deQueueStat;
    }

    public EnQueueStat getEnQueueStat() {
        return enQueueStat;
    }

    public ConcurrentMap<String, TopicStat> getTopicStats() {
        return topicStatMap;
    }

    public ReplicationStat getReplicationStat() {
        return replicationStat;
    }

    public Integer getBrokerId() {
        return brokerId;
    }
}
