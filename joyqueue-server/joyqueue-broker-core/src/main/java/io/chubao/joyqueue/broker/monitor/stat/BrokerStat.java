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
