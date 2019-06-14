/**
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
package com.jd.journalq.broker.kafka.coordinator.delay;

import com.jd.journalq.broker.kafka.coordinator.GroupBalanceManager;
import com.jd.journalq.broker.kafka.coordinator.domain.KafkaCoordinatorGroup;
import com.jd.journalq.broker.kafka.coordinator.domain.KafkaCoordinatorGroupMember;
import com.jd.journalq.toolkit.delay.DelayedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DelayedHeartbeat
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/7
 */
public class DelayedHeartbeat extends DelayedOperation {

    protected static final Logger logger = LoggerFactory.getLogger(DelayedHeartbeat.class);

    private GroupBalanceManager groupBalanceManager;
    private KafkaCoordinatorGroup group;
    private KafkaCoordinatorGroupMember member;
    private long heartbeatDeadline;

    public DelayedHeartbeat(GroupBalanceManager groupBalanceManager, KafkaCoordinatorGroup group, KafkaCoordinatorGroupMember member, long heartbeatDeadline,
                            long sessionTimeout) {
        super(sessionTimeout);
        this.groupBalanceManager = groupBalanceManager;
        this.group = group;
        this.member = member;
        this.heartbeatDeadline = heartbeatDeadline;
    }

    @Override
    protected boolean tryComplete() {
        synchronized (group) {
            if (groupBalanceManager.shouldKeepMemberAlive(member, heartbeatDeadline) || member.isLeaving()) {
                return forceComplete();
            } else {
                return false;
            }
        }
    }

    @Override
    protected void onExpiration() {
        synchronized (group) {
            logger.info("group {} Member {} heartbeat expired, join callback = {}, sync callback = {}",
                    group.getId(), member.getId(), member.getAwaitingJoinCallback(), member.getAwaitingSyncCallback());

            if (!groupBalanceManager.shouldKeepMemberAlive(member, heartbeatDeadline)) {
                groupBalanceManager.removeMemberAndUpdateGroup(group, member);
                group.addExpiredMember(member);
            }
        }
    }

    @Override
    protected void onComplete() {
    }
}
