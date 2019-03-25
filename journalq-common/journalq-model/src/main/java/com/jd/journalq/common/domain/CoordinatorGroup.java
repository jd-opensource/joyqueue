package com.jd.journalq.common.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author  wangjin18
 * @date    2019-01-23
 *
 **/
public class CoordinatorGroup {
    private String id;
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
}
