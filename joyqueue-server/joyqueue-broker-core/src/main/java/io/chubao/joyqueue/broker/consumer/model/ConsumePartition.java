package io.chubao.joyqueue.broker.consumer.model;

import org.apache.commons.lang3.StringUtils;

/**
 * 消费分区
 * <p>
 * Created by chengzhiliang on 2018/8/20.
 */
public class ConsumePartition implements Cloneable{
    // 主题
    private String topic;
    // 应用
    private String app;
    // 分区编号
    private short partition;
    // 分区分组
    private int partitionGroup;
    // 连接唯一标示
    private String connectionId;

    public ConsumePartition(){}

    public ConsumePartition(String topic, String app) {
        this.topic = topic;
        this.app = app;
    }

    public ConsumePartition(String topic, String app, short partition) {
        this.topic = topic;
        this.app = app;
        this.partition = partition;
    }

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

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public int hashCode() {
        int result = topic.hashCode();
        result = 31 * result + app.hashCode();
        result = 31 * result + (partition ^ partition >>> 32);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        ConsumePartition that = (ConsumePartition) obj;
        if (!StringUtils.equals(this.topic, that.topic)) {
            return false;
        }
        if (!StringUtils.equals(this.app, that.app)) {
            return false;
        }
        if (this.partition != that.partition) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConsumePartition{");
        sb.append("topic='").append(topic).append('\'');
        sb.append(", app=").append(app);
        sb.append(", partition=").append(partition);
        sb.append('}');
        return sb.toString();
    }
}
