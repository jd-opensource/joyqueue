package com.jd.journalq.broker.jmq.coordinator.domain;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.coordinator.domain.CoordinatorGroup;
import com.jd.journalq.broker.coordinator.domain.CoordinatorGroupMember;
import org.apache.commons.collections.MapUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * JMQCoordinatorGroup
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class JMQCoordinatorGroup extends CoordinatorGroup {

    private String assignType;
    private Map<String, Object> assignContext;

    public JMQCoordinatorGroup() {

    }

    public JMQCoordinatorGroup(String id) {
        super(id);
    }

    public JMQCoordinatorGroupMember getMember(String id) {
        return (JMQCoordinatorGroupMember) getMembers().get(id);
    }

    public List<JMQCoordinatorGroupMember> getMemberList() {
        ConcurrentMap<String, CoordinatorGroupMember> members = getMembers();
        if (MapUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<JMQCoordinatorGroupMember> result = Lists.newArrayListWithExpectedSize(members.size());
        for (Map.Entry<String, CoordinatorGroupMember> entry : members.entrySet()) {
            result.add((JMQCoordinatorGroupMember) entry.getValue());
        }
        return result;
    }

    public void setAssignContext(Map<String, Object> assignContext) {
        this.assignContext = assignContext;
    }

    public Map<String, Object> getAssignContext() {
        return assignContext;
    }

    public void setAssignType(String assignType) {
        this.assignType = assignType;
    }

    public String getAssignType() {
        return assignType;
    }

    protected void updateExtension() {
        super.setExtension(String.format("{assignType: %s}", assignType));
    }
}