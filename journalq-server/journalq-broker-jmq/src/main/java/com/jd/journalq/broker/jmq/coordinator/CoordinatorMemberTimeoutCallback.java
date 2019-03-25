package com.jd.journalq.broker.jmq.coordinator;

import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroup;
import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroupMember;

/**
 * CoordinatorMemberTimeoutCallback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/6
 */
public interface CoordinatorMemberTimeoutCallback {

    void onCompletion(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member);
}