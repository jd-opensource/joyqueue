package com.jd.journalq.model.query;

import com.jd.journalq.common.model.Query;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.Topic;

public class QPartitionGroupReplica implements Query {
    private Topic topic;
    private Namespace namespace;
    private int groupNo = -1;

    public QPartitionGroupReplica() {
    }

    public QPartitionGroupReplica(Topic topic) {
        this.topic = topic;
    }

    public QPartitionGroupReplica(Topic topic, Namespace namespace) {
        this.topic = topic;
        this.namespace = namespace;
    }

    public QPartitionGroupReplica(Topic topic, int groupNo) {
        this.topic = topic;
        this.groupNo = groupNo;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public int getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(int groupNo) {
        this.groupNo = groupNo;
    }
}
