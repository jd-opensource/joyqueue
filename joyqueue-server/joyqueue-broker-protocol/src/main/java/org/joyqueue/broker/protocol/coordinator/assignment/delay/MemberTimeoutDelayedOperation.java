/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.protocol.coordinator.assignment.delay;

import org.joyqueue.broker.protocol.coordinator.domain.GroupMemberMetadata;
import org.joyqueue.broker.protocol.coordinator.domain.GroupMetadata;
import org.joyqueue.toolkit.delay.AbstractDelayedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MemberTimeoutDelayedOperation
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class MemberTimeoutDelayedOperation extends AbstractDelayedOperation {

    protected static final Logger logger = LoggerFactory.getLogger(MemberTimeoutDelayedOperation.class);

    private GroupMetadata group;
    private GroupMemberMetadata member;

    public MemberTimeoutDelayedOperation(GroupMetadata group, GroupMemberMetadata member, long delayMs) {
        super(delayMs);
        this.group = group;
        this.member = member;
    }

    @Override
    protected boolean tryComplete() {
        if (member.isExpired()) {
            return forceComplete();
        } else {
            return false;
        }
    }

    @Override
    protected void onExpiration() {
        if (!group.getMembers().containsKey(member.getId()) || !member.isExpired()) {
            return;
        }

        logger.info("joyqueue consumer {} is expired, release assigned partition, connection: {}, latestHeartbeat: {}", member.getId(), member.getConnectionHost(), member.getLatestHeartbeat());

        if (member.getTimeoutCallback() != null) {
            member.getTimeoutCallback().onCompletion(group, member);
        }
        group.addExpiredMember(member);
        group.getMembers().remove(member.getId(), member);
    }
}