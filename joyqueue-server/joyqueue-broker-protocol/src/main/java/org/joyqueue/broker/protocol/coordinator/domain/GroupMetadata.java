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
package org.joyqueue.broker.protocol.coordinator.domain;

import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * GroupMetadata
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class GroupMetadata extends org.joyqueue.broker.coordinator.group.domain.GroupMetadata {

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
        ConcurrentMap<String, org.joyqueue.broker.coordinator.group.domain.GroupMemberMetadata> members = getMembers();
        if (MapUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<GroupMemberMetadata> result = Lists.newArrayListWithExpectedSize(members.size());
        for (Map.Entry<String, org.joyqueue.broker.coordinator.group.domain.GroupMemberMetadata> entry : members.entrySet()) {
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