package com.jd.journalq.broker.jmq.coordinator.domain;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.coordinator.group.domain.GroupMemberMetadata;
import org.apache.commons.collections.MapUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * GroupMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class GroupMetadata extends com.jd.journalq.broker.coordinator.group.domain.GroupMetadata {

    private String assignType;
    private Map<String, Object> assignContext;

    public GroupMetadata() {

    }

    public GroupMetadata(String id) {
        super(id);
    }

    public com.jd.journalq.broker.jmq.coordinator.domain.GroupMemberMetadata getMember(String id) {
        return (com.jd.journalq.broker.jmq.coordinator.domain.GroupMemberMetadata) getMembers().get(id);
    }

    public List<com.jd.journalq.broker.jmq.coordinator.domain.GroupMemberMetadata> getMemberList() {
        ConcurrentMap<String, GroupMemberMetadata> members = getMembers();
        if (MapUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<com.jd.journalq.broker.jmq.coordinator.domain.GroupMemberMetadata> result = Lists.newArrayListWithExpectedSize(members.size());
        for (Map.Entry<String, GroupMemberMetadata> entry : members.entrySet()) {
            result.add((com.jd.journalq.broker.jmq.coordinator.domain.GroupMemberMetadata) entry.getValue());
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