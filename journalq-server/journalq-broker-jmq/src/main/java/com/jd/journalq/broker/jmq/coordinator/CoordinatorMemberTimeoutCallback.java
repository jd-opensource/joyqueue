package com.jd.journalq.broker.jmq.coordinator;

import com.jd.journalq.broker.jmq.coordinator.domain.GroupMemberMetadata;
import com.jd.journalq.broker.jmq.coordinator.domain.GroupMetadata;

/**
 * CoordinatorMemberTimeoutCallback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/6
 */
public interface CoordinatorMemberTimeoutCallback {

    void onCompletion(GroupMetadata group, GroupMemberMetadata member);
}