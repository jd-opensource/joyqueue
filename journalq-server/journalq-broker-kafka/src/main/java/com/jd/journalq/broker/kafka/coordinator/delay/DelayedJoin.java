package com.jd.journalq.broker.kafka.coordinator.delay;

import com.jd.journalq.broker.kafka.coordinator.KafkaCoordinatorGroupManager;
import com.jd.journalq.broker.kafka.coordinator.domain.GroupJoinGroupResult;
import com.jd.journalq.broker.kafka.coordinator.domain.GroupState;
import com.jd.journalq.broker.kafka.coordinator.domain.KafkaCoordinatorGroup;
import com.jd.journalq.broker.kafka.coordinator.domain.KafkaCoordinatorGroupMember;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.coordinator.GroupBalanceManager;
import com.jd.journalq.toolkit.delay.DelayedOperation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DelayedJoin
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/7
 */
public class DelayedJoin extends DelayedOperation {

    protected static final Logger logger = LoggerFactory.getLogger(DelayedJoin.class);

    private GroupBalanceManager groupBalanceManager;
    private KafkaCoordinatorGroupManager groupMetadataManager;
    private KafkaCoordinatorGroup group;

    public DelayedJoin(GroupBalanceManager groupBalanceManager, KafkaCoordinatorGroupManager groupMetadataManager, KafkaCoordinatorGroup group, long rebalanceTimeout) {
        super(rebalanceTimeout);
        this.groupBalanceManager = groupBalanceManager;
        this.groupMetadataManager = groupMetadataManager;
        this.group = group;
    }

    @Override
    protected boolean tryComplete() {
        synchronized (group) {
            if (group.getNotYetRejoinedMembers().isEmpty()) {
                return forceComplete();
            } else {
                return false;
            }
        }
    }

    @Override
    protected void onExpiration() {
        logger.info("group {} expire join.", group.getId());
    }

    @Override
    protected void onComplete() {
        synchronized (group) {
            doComplete();
        }
    }

    protected void doComplete() {
        // TODO 临时日志
        logger.info("group {} delay join", group.getId());

        List<KafkaCoordinatorGroupMember> failedMembers = group.getNotYetRejoinedMembers();
        // 移除未join的member
        if (CollectionUtils.isNotEmpty(failedMembers)) {
            for (KafkaCoordinatorGroupMember memberMetadata : failedMembers) {
                logger.info("group {} complete join, member {} not join.", group.getId(), memberMetadata.getId());
                group.removeMember(memberMetadata.getId());
            }
        }

        // 如果member为空，那么移除group
        if (group.isMemberEmpty()) {
            logger.info("group {} generation {} is dead and removed", group.getId(), group.getGenerationId());
            group.transitionStateTo(GroupState.DEAD);
            groupMetadataManager.removeGroup(group);
            return;
        }

        // join成功
        if (!group.stateIs(GroupState.DEAD)) {
            group.initNextGeneration();
            logger.info("stabilized group {} generation {}, member count is {}", group.getId(), group.getGenerationId(), group.getAllMemberIds().size());

            for (KafkaCoordinatorGroupMember memberMetadata : group.getAllMembers()) {
                Map<String, byte[]> members = null;
                if (memberMetadata.getId().equals(group.getLeaderId())) {
                    members = group.currentMemberMetadata();
                } else {
                    members = Collections.emptyMap();
                }

                GroupJoinGroupResult groupJoinGroupResult = new GroupJoinGroupResult(members, memberMetadata.getId(), group.getGenerationId(), group.getProtocol(),
                        group.getLeaderId(), KafkaErrorCode.NONE.getCode());
                memberMetadata.getAwaitingJoinCallback().sendResponseCallback(groupJoinGroupResult);
                memberMetadata.setAwaitingJoinCallback(null);

                groupBalanceManager.completeAndScheduleNextHeartbeatExpiration(group, memberMetadata);
            }
        }
    }
}

