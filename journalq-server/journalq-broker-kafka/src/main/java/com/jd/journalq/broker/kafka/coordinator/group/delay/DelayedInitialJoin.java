package com.jd.journalq.broker.kafka.coordinator.group.delay;


/**
 * Created by zhuduohui on 2018/4/3.
 */

import com.google.common.collect.Sets;
import com.jd.journalq.broker.kafka.coordinator.group.GroupBalanceManager;
import com.jd.journalq.broker.kafka.coordinator.group.GroupMetadataManager;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMetadata;
import com.jd.journalq.toolkit.delay.DelayedOperationKey;
import com.jd.journalq.toolkit.delay.DelayedOperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Delayed rebalance operation that is added to the purgatory when a group is transitioning from
 * Empty to PreparingRebalance
 * <p>
 * When onComplete is triggered we check if any new members have been added and if there is still time remaining
 * before the rebalance timeout. If both are true we then schedule a further delay. Otherwise we complete the
 * rebalance.
 */
public class DelayedInitialJoin extends DelayedJoin {
    private static Logger logger = LoggerFactory.getLogger(DelayedInitialJoin.class);

    private GroupBalanceManager groupBalanceManager;
    private GroupMetadataManager groupMetadataManager;
    private GroupMetadata group;
    private DelayedOperationManager<DelayedJoin> joinPurgatory;
    private long configurationRebalanceDelay;
    private long delayMs;
    private long remainingMs;

    public DelayedInitialJoin(GroupBalanceManager groupBalanceManager, GroupMetadataManager groupMetadataManager, GroupMetadata group,
                              DelayedOperationManager<DelayedJoin> joinPurgatory, long configuredRebalanceDelay, long delayMs, long remainingMs) {
        super(groupBalanceManager, groupMetadataManager, group, delayMs);
        this.groupBalanceManager = groupBalanceManager;
        this.groupMetadataManager = groupMetadataManager;
        this.group = group;
        this.joinPurgatory = joinPurgatory;
        this.delayMs = delayMs;
        this.remainingMs = remainingMs;
        this.configurationRebalanceDelay = configuredRebalanceDelay;
    }

    @Override
    protected boolean tryComplete() {
        return false;
    }

    @Override
    protected void onComplete() {
        logger.info("delayed initial join onComplete, isNewMemberAdded = {}, remainingMs = {}, delayMs = {}, configurationRebalanceDelay = {}",
                group.isNewMemberAdded(), remainingMs, delayMs, configurationRebalanceDelay);

        synchronized (group) {
            if (group.isNewMemberAdded() && remainingMs != 0) {
                group.setNewMemberAdded(false);
                long delay = Math.min(configurationRebalanceDelay, remainingMs);
                long remaining = Math.max(remainingMs - delayMs, 0);
                DelayedOperationKey groupKey = new DelayedOperationKey(group.getId());
                Set<Object> delayedOperationKeys = Sets.newHashSet(groupKey);
                joinPurgatory.tryCompleteElseWatch(new DelayedInitialJoin(groupBalanceManager, groupMetadataManager,
                        group, joinPurgatory, configurationRebalanceDelay, delay, remaining), delayedOperationKeys);
            } else {
                super.onComplete();
            }
        }
    }
}
