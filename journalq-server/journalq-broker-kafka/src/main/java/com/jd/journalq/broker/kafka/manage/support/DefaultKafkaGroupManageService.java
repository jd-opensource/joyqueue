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
                groupMemberMetadata.getAwaitingJoinCallback().sendResponseCallback(GroupJoinGroupResult.buildError(groupMemberMetadata.getId(), KafkaErrorCode.UNKNOWN_MEMBER_ID));
            }
            if (groupMemberMetadata.getAwaitingSyncCallback() != null) {
                groupMemberMetadata.getAwaitingSyncCallback().sendResponseCallback(null, KafkaErrorCode.UNKNOWN_MEMBER_ID);
            }
        }

        return true;
    }
}