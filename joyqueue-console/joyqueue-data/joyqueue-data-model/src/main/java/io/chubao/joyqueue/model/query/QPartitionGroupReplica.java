package io.chubao.joyqueue.model.query;

import io.chubao.joyqueue.model.QKeyword;
import io.chubao.joyqueue.model.Query;
import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.domain.Topic;

public class QPartitionGroupReplica  extends QKeyword implements Query {
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
