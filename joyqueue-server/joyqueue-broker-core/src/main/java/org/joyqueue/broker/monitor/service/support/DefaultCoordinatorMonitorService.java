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
package org.joyqueue.broker.monitor.service.support;

import com.google.common.collect.Maps;
import org.joyqueue.broker.coordinator.CoordinatorService;
import org.joyqueue.broker.coordinator.domain.CoordinatorDetail;
import org.joyqueue.broker.coordinator.group.GroupMetadataManager;
import org.joyqueue.broker.coordinator.group.domain.GroupMemberMetadata;
import org.joyqueue.broker.coordinator.group.domain.GroupMetadata;
import org.joyqueue.broker.monitor.service.CoordinatorMonitorService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * DefaultCoordinatorMonitorService
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class DefaultCoordinatorMonitorService implements CoordinatorMonitorService {

    private CoordinatorService coordinatorService;

    public DefaultCoordinatorMonitorService(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    @Override
    public CoordinatorDetail getCoordinator(String groupId) {
        return coordinatorService.getCoordinator().getGroupDetail(groupId);
    }

    @Override
    public GroupMetadata getCoordinatorGroup(String namespace, String groupId, String topic, boolean isFormat) {
        GroupMetadataManager groupMetadataManager = coordinatorService.getOrCreateGroupMetadataManager(namespace);
        GroupMetadata group = groupMetadataManager.getGroup(groupId);
        if (group == null) {
            return null;
        }
        if (!isFormat) {
            return group;
        }
        GroupMetadata result = new GroupMetadata();
        result.setId(group.getId());
        result.setExtension(group.getExtension());
        result.setMembers(formatCoordinatorGroupMembers(group, topic));
        result.setExpiredMembersMap(Maps.newHashMap(group.expiredMembersToMap()));
        return result;
    }

    @Override
    public Map<String, GroupMemberMetadata> getCoordinatorGroupMembers(String namespace, String groupId, String topic, boolean isFormat) {
        GroupMetadataManager groupMetadataManager = coordinatorService.getOrCreateGroupMetadataManager(namespace);
        GroupMetadata group = groupMetadataManager.getGroup(groupId);
        if (group == null) {
            return null;
        }
        if (!isFormat) {
            return group.getMembers();
        }
        return formatCoordinatorGroupMembers(group, topic);
    }

    protected ConcurrentMap<String, GroupMemberMetadata> formatCoordinatorGroupMembers(GroupMetadata group, String topic) {
        if (MapUtils.isEmpty(group.getMembers())) {
            return null;
        }
        ConcurrentMap<String, GroupMemberMetadata> result = Maps.newConcurrentMap();
        for (Map.Entry<String, GroupMemberMetadata> entry : group.getMembers().entrySet()) {
            GroupMemberMetadata sourceMember = entry.getValue();

            if (StringUtils.isNotBlank(topic) && sourceMember.getAssignments() != null && !sourceMember.getAssignments().containsKey(topic)) {
                continue;
            }

            GroupMemberMetadata member = new GroupMemberMetadata();
            member.setId(sourceMember.getId());
            member.setGroupId(sourceMember.getGroupId());
            member.setConnectionId(sourceMember.getConnectionId());
            member.setConnectionHost(sourceMember.getConnectionHost());
            member.setLatestHeartbeat(sourceMember.getLatestHeartbeat());
            member.setSessionTimeout(sourceMember.getSessionTimeout());

            if (StringUtils.isBlank(topic)) {
                member.setAssignments(sourceMember.getAssignments());
            } else if (MapUtils.isNotEmpty(sourceMember.getAssignments())) {
                member.setAssignmentList(sourceMember.getAssignments().get(topic));
            }
            result.put(entry.getKey(), member);
        }
        return result;
    }
}