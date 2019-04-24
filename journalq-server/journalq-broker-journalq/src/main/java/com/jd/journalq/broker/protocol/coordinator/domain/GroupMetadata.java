package com.jd.journalq.broker.protocol.coordinator.domain;

import com.google.common.collect.Lists;
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

    public GroupMemberMetadata getMember(String id) {
        return (GroupMemberMetadata) getMembers().get(id);
    }

    public List<GroupMemberMetadata> getMemberList() {
        ConcurrentMap<String, com.jd.journalq.broker.coordinator.group.domain.GroupMemberMetadata> members = getMembers();
        if (MapUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<GroupMemberMetadata> result = Lists.newArrayListWithExpectedSize(members.size());
        for (Map.Entry<String, com.jd.journalq.broker.coordinator.group.domain.GroupMemberMetadata> entry : members.entrySet()) {
            result.add((GroupMemberMetadata) entry.getValue());
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