package com.jd.journalq.nsr.model;

import com.jd.journalq.model.Query;

public class PartitionGroupQuery implements Query {
    /**
     * 主题
     */
    private String topic;
    /**
     * 命名空间
     */
    private String namespace;
    /**
     * 分组
     */
    private int group = -1;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }


    public PartitionGroupQuery(String topic, String namespace) {
        this.topic = topic;
        this.namespace = namespace;
    }

    public PartitionGroupQuery(String topic, String namespace, int group) {
        this.topic = topic;
        this.namespace = namespace;
        this.group = group;
    }

    public PartitionGroupQuery() {
    }
}
