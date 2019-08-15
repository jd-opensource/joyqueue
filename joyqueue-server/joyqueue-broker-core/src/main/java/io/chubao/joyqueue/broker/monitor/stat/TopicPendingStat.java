package io.chubao.joyqueue.broker.monitor.stat;

import io.chubao.joyqueue.broker.monitor.PendingStat;

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
