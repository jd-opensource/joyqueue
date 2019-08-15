package io.chubao.joyqueue.monitor;

/**
 * ReplicationMonitorInfo
 *
 * author: gaohaoxiang
 * date: 2018/11/16
 */
public class ReplicationMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private int partitionGroup;
    private EnQueueMonitorInfo replicaStat;
    private EnQueueMonitorInfo appendStat;
    private boolean started;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public EnQueueMonitorInfo getReplicaStat() {
        return replicaStat;
    }

    public void setReplicaStat(EnQueueMonitorInfo replicaStat) {
        this.replicaStat = replicaStat;
    }

    public EnQueueMonitorInfo getAppendStat() {
        return appendStat;
    }

    public void setAppendStat(EnQueueMonitorInfo appendStat) {
        this.appendStat = appendStat;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isStarted() {
        return started;
    }
}