package com.jd.journalq.common.network.command;

/**
 * FetchAssignedPartitionData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class FetchAssignedPartitionData {

    private String topic;
    private int sessionTimeout;
    private boolean nearby;

    public FetchAssignedPartitionData() {

    }

    public FetchAssignedPartitionData(String topic, int sessionTimeout, boolean nearby) {
        this.topic = topic;
        this.sessionTimeout = sessionTimeout;
        this.nearby = nearby;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public boolean isNearby() {
        return nearby;
    }

    public void setNearby(boolean nearby) {
        this.nearby = nearby;
    }

    @Override
    public String toString() {
        return "FetchAssignedPartitionData{" +
                "topic='" + topic + '\'' +
                ", sessionTimeout=" + sessionTimeout +
                ", nearby=" + nearby +
                '}';
    }
}