package io.chubao.joyqueue.domain;

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
