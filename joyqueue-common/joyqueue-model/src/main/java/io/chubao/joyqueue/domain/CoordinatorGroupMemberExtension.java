package io.chubao.joyqueue.domain;

import java.util.List;

public class CoordinatorGroupMemberExtension {
    private List<CoordinatorGroupMember> members;
    private String extension;

    public List<CoordinatorGroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<CoordinatorGroupMember> members) {
        this.members = members;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
