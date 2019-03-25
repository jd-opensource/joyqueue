package com.jd.journalq.model.query;

import com.jd.journalq.common.model.Query;

import java.util.List;

public class QTopicGroupMaster implements Query {
    private String namespace;
    private String topic;
    private List<Integer> groups;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<Integer> getGroups() {
        return groups;
    }
    public void setGroups(List<Integer> groups) {
        this.groups = groups;
    }
}
