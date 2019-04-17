package com.jd.journalq.broker.kafka.manage.support;

import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupJoinGroupResult;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMetadata;
import com.jd.journalq.broker.kafka.manage.KafkaGroupManageService;

/**
 * DefaultKafkaGroupManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/13
 */
public class DefaultKafkaGroupManageService implements KafkaGroupManageService {

    private GroupCoordinator groupCoordinator;

    public DefaultKafkaGroupManageService(GroupCoordinator groupCoordinator) {
        this.groupCoordinator = groupCoordinator;
    }

    @Override
    public boolean removeGroup(String groupId) {
        GroupMetadata group = groupCoordinator.getGroup(groupId);
        if (group == null) {
            return false;
        }
        return groupCoordinator.removeGroup(group);
    }

    @Override
    public boolean rebalanceGroup(String groupId) {
        GroupMetadata group = groupCoordinator.getGroup(groupId);
        if (group == null) {
            return false;
        }

        groupCoordinator.removeGroup(group);

        for (GroupMemberMetadata groupMemberMetadata : group.getAllMembers()) {
            if (groupMemberMetadata.getAwaitingJoinCallback() != null) {
                groupMemberMetadata.getAwaitingJoinCallback().sendResponseCallback(GroupJoinGroupResult.buildError(groupMemberMetadata.getId(), KafkaErrorCode.UNKNOWN_MEMBER_ID.getCode()));
                groupMemberMetadata.setAwaitingJoinCallback(null);
            }
            if (groupMemberMetadata.getAwaitingSyncCallback() != null) {
                groupMemberMetadata.getAwaitingSyncCallback().sendResponseCallback(null, KafkaErrorCode.UNKNOWN_MEMBER_ID.getCode());
                groupMemberMetadata.setAwaitingSyncCallback(null);
            }
        }

        return true;
    }
}