package io.chubao.joyqueue.client.internal.consumer.coordinator.domain;

import io.chubao.joyqueue.toolkit.time.SystemClock;

/**
 * BrokerAssignmentsHolder
 *
 * author: gaohaoxiang
 * date: 2018/12/11
 */
public class BrokerAssignmentsHolder {

    private BrokerAssignments brokerAssignments;
    private long createTime;

    public BrokerAssignmentsHolder(BrokerAssignments brokerAssignments, long createTime) {
        this.brokerAssignments = brokerAssignments;
        this.createTime = createTime;
    }

    public boolean isExpired(long expireTime) {
        return (createTime + expireTime < SystemClock.now());
    }

    public BrokerAssignments getBrokerAssignments() {
        return brokerAssignments;
    }

    public void setBrokerAssignments(BrokerAssignments brokerAssignments) {
        this.brokerAssignments = brokerAssignments;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}