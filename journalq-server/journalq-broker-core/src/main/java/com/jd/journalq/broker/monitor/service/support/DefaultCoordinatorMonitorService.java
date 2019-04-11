package com.jd.journalq.broker.monitor.service.support;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.coordinator.CoordinatorService;
import com.jd.journalq.broker.coordinator.domain.CoordinatorDetail;
import com.jd.journalq.broker.coordinator.group.GroupMetadataManager;
import com.jd.journalq.broker.coordinator.group.domain.GroupMemberMetadata;
import com.jd.journalq.broker.coordinator.group.domain.GroupMetadata;
import com.jd.journalq.broker.monitor.service.CoordinatorMonitorService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * DefaultCoordinatorMonitorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class DefaultCoordinatorMonitorService implements CoordinatorMonitorService {

    private CoordinatorService coordinatorService;

    public DefaultCoordinatorMonitorService(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    @Override
    public CoordinatorDetail getCoordinator(String groupId) {
        return coordinatorService.getCoordinator().getGroupCoordinatorDetail(groupId);
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

            if (StringUtils.isNotBlank(topic) && !sourceMember.getAssignments().containsKey(topic)) {
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