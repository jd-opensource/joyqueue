package io.chubao.joyqueue.broker.protocol.coordinator;

import io.chubao.joyqueue.broker.protocol.coordinator.domain.GroupMemberMetadata;
import io.chubao.joyqueue.broker.protocol.coordinator.domain.GroupMetadata;

/**
 * GroupMemberTimeoutCallback
 *
 * author: gaohaoxiang
 * date: 2018/12/6
 */
public interface GroupMemberTimeoutCallback {

    void onCompletion(GroupMetadata group, GroupMemberMetadata member);
}
