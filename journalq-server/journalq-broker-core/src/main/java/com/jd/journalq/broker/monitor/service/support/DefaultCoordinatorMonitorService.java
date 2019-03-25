package com.jd.journalq.broker.monitor.service.support;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.coordinator.CoordinatorGroupManager;
import com.jd.journalq.broker.coordinator.CoordinatorService;
import com.jd.journalq.broker.coordinator.domain.CoordinatorDetail;
import com.jd.journalq.broker.coordinator.domain.CoordinatorGroup;
import com.jd.journalq.broker.coordinator.domain.CoordinatorGroupMember;
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
        return coordinatorService.getCoordinator().getCoordinatorDetail(groupId);
    }

    @Override
    public CoordinatorGroup getCoordinatorGroup(String namespace, String groupId, String topic, boolean isFormat) {
        CoordinatorGroupManager coordinatorGroupManager = coordinatorService.getOrCreateCoordinatorGroupManager(namespace);
        CoordinatorGroup group = coordinatorGroupManager.getGroup(groupId);
        if (group == null) {
            return null;
        }
        if (!isFormat) {
            return group;
        }
        CoordinatorGroup result = new CoordinatorGroup();
        result.setId(group.getId());
        result.setMembers(formatCoordinatorGroupMembers(group, topic));
        result.setExpiredMembersMap(Maps.newHashMap(group.expiredMembersToMap()));
        return result;
    }

    @Override
    public Map<String, CoordinatorGroupMember> getCoordinatorGroupMembers(String namespace, String groupId, String topic, boolean isFormat) {
        CoordinatorGroupManager coordinatorGroupManager = coordinatorService.getOrCreateCoordinatorGroupManager(namespace);
        CoordinatorGroup group = coordinatorGroupManager.getGroup(groupId);
        if (group == null) {
            return null;
        }
        if (!isFormat) {
            return group.getMembers();
        }
        return formatCoordinatorGroupMembers(group, topic);
    }

    protected ConcurrentMap<String, CoordinatorGroupMember> formatCoordinatorGroupMembers(CoordinatorGroup group, String topic) {
        if (MapUtils.isEmpty(group.getMembers())) {
            return null;
        }
        ConcurrentMap<String, CoordinatorGroupMember> result = Maps.newConcurrentMap();
        for (Map.Entry<String, CoordinatorGroupMember> entry : group.getMembers().entrySet()) {
            CoordinatorGroupMember sourceMember = entry.getValue();

            if (StringUtils.isNotBlank(topic) && !sourceMember.getAssignments().containsKey(topic)) {
                continue;
            }

            CoordinatorGroupMember member = new CoordinatorGroupMember();
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