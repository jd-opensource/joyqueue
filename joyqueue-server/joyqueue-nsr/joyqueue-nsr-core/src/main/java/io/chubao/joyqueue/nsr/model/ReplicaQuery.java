package io.chubao.joyqueue.nsr.model;

import io.chubao.joyqueue.model.Query;

public class ReplicaQuery implements Query {
    private String topic;
    private String namespace;
    private int group = -1;
    private int brokerId;

    public ReplicaQuery() {
    }

    public ReplicaQuery(String topic, String namespace, int group, int brokerId) {
        this.topic = topic;
        this.namespace = namespace;
        this.group = group;
        this.brokerId = brokerId;
    }

    public ReplicaQuery(String topic, String namespace, int group) {
        this.topic = topic;
        this.namespace = namespace;
        this.group = group;
    }


    public ReplicaQuery(int brokerId) {
        this.brokerId = brokerId;
    }

    public ReplicaQuery(String topic, String namespace) {
        this.topic = topic;
        this.namespace = namespace;
    }

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

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }
}
