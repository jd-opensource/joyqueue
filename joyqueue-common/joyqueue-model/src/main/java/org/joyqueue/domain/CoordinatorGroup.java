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
package org.joyqueue.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * 2019-01-23
 * @author  wangjin18
 *
 **/
public class CoordinatorGroup {
    private String id;
    private String extension;
    private ConcurrentMap<String, CoordinatorGroupMember> members;
    private Map<String, CoordinatorGroupExpiredMember> expiredMembers ;
    private transient Map<String, CoordinatorGroupExpiredMember> expiredMembersMap;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConcurrentMap<String, CoordinatorGroupMember> getMembers() {
        return members;
    }

    public void setMembers(ConcurrentMap<String, CoordinatorGroupMember> members) {
        this.members = members;
    }


    public Map<String, CoordinatorGroupExpiredMember> getExpiredMembers() {
        return expiredMembers;
    }

    public void setExpiredMembers(Map<String, CoordinatorGroupExpiredMember> expiredMembers) {
        this.expiredMembers = expiredMembers;
    }

    public Map<String, CoordinatorGroupExpiredMember> getExpiredMembersMap() {
        return expiredMembersMap;
    }

    public void setExpiredMembersMap(Map<String, CoordinatorGroupExpiredMember> expiredMembersMap) {
        this.expiredMembersMap = expiredMembersMap;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
