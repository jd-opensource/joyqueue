/**
 * Partially copied from Apache Kafka.
 *
 * Original LICENSE :
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.kafka.coordinator.group.delay;

import org.joyqueue.broker.kafka.coordinator.group.GroupBalanceManager;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMemberMetadata;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMetadata;
import org.joyqueue.toolkit.delay.DelayedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayedHeartbeat extends DelayedOperation {

    protected static final Logger logger = LoggerFactory.getLogger(DelayedHeartbeat.class);

    private GroupBalanceManager groupBalanceManager;
    private GroupMetadata group;
    private GroupMemberMetadata member;
    private long heartbeatDeadline;

    public DelayedHeartbeat(GroupBalanceManager groupBalanceManager, GroupMetadata group, GroupMemberMetadata member, long heartbeatDeadline,
                            long sessionTimeout) {
        super(sessionTimeout, group.getLock());
        this.groupBalanceManager = groupBalanceManager;
        this.group = group;
        this.member = member;
        this.heartbeatDeadline = heartbeatDeadline;
    }

    @Override
    protected boolean tryComplete() {
        return group.inLock(() -> {
            if (groupBalanceManager.shouldKeepMemberAlive(member, heartbeatDeadline) || member.isLeaving()) {
                return forceComplete();
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onExpiration() {
        group.inLock(() -> {
            if (!groupBalanceManager.shouldKeepMemberAlive(member, heartbeatDeadline)) {
                logger.info("group {} Member {} heartbeat expired, join callback = {}, sync callback = {}",
                        group.getId(), member.getId(), member.getAwaitingJoinCallback(), member.getAwaitingSyncCallback());

                groupBalanceManager.removeMemberAndUpdateGroup(group, member);
                group.addExpiredMember(member);
            }
        });
    }

    @Override
    protected void onComplete() {
    }
}
