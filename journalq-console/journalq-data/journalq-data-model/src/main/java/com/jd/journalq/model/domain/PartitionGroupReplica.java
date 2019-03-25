package com.jd.journalq.model.domain;

import com.jd.journalq.model.domain.nsr.BaseNsrModel;

public class PartitionGroupReplica  extends BaseNsrModel implements Comparable<PartitionGroupReplica> {

    public static final int ROLE_DYNAMIC = 0;

    public static final int ROLE_MASTER = 1;

    public static final int ROLE_SLAVE = 2;

    public static final int ROLE_LEARNER = 3;
    //信息不同步
    public static final int STATE_OUT_SYNC = 2;
    private Namespace namespace;
    private Topic topic;
    private int groupNo;
    private int brokerId;
    private int role = ROLE_DYNAMIC;
    private Broker broker;

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public int getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(int groupNo) {
        this.groupNo = groupNo;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    @Override
    public int compareTo(PartitionGroupReplica o) {
        return this.getBrokerId()-o.getBrokerId();
    }
}
