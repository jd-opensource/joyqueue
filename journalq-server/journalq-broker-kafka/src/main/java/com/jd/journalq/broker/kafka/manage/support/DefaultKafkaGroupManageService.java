package com.jd.journalq.broker.kafka.manage.support;

import com.jd.journalq.broker.kafka.coordinator.KafkaCoordinatorGroupManager;
import com.jd.journalq.broker.kafka.coordinator.domain.GroupJoinGroupResult;
import com.jd.journalq.broker.kafka.coordinator.domain.KafkaCoordinatorGroup;
import com.jd.journalq.broker.kafka.coordinator.domain.KafkaCoordinatorGroupMember;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.manage.KafkaGroupManageService;

/**
 * DefaultKafkaGroupManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/13
 */
public class DefaultKafkaGroupManageService implements KafkaGroupManageService {

    private KafkaCoordinatorGroupManager groupMetadataManager;

    public DefaultKafkaGroupManageService(KafkaCoordinatorGroupManager groupMetadataManager) {
        this.groupMetadataManager = groupMetadataManager;
    }

    @Override
    public boolean removeGroup(String groupId) {
        KafkaCoordinatorGroup group = groupMetadataManager.getGroup(groupId);
        if (group == null) {
            return false;
        }
        return groupMetadataManager.removeGroup(group);
    }

    @Override
    public boolean rebalanceGroup(String groupId) {
        KafkaCoordinatorGroup group = groupMetadataManager.getGroup(groupId);
        if (group == null) {
            return false;
        }

        groupMetadataManager.removeGroup(group);

        for (KafkaCoordinatorGroupMember groupMemberMetadata : group.getAllMembers()) {
            if (groupMemberMetadata.getAwaitingJoinCallback() != null) {
                groupMemberMetadata.getAwaitingJoinCallback().sendResponseCallback(GroupJoinGroupResult.buildError(groupMemberMetadata.getId(), KafkaErrorCode.UNKNOWN_MEMBER_ID.getCode()));
            }
            if (groupMemberMetadata.getAwaitingSyncCallback() != null) {
                groupMemberMetadata.getAwaitingSyncCallback().sendResponseCallback(null, KafkaErrorCode.UNKNOWN_MEMBER_ID.getCode());
            }
        }

        return true;
    }
}