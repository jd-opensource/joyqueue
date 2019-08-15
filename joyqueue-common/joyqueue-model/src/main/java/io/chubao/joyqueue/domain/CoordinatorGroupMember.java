package io.chubao.joyqueue.domain;

import io.chubao.joyqueue.toolkit.time.SystemClock;

import java.beans.Transient;
import java.util.List;

/**
 * CoordinatorGroupMember
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class CoordinatorGroupMember {
    private String id;
    private String groupId;
    private String connectionId;
    private String connectionHost;
    private long latestHeartbeat;
    private int sessionTimeout;
    List<Short> assignmentList;
    @Transient
    public  boolean isExpired() {
        return (latestHeartbeat + sessionTimeout) < SystemClock.now();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getConnectionHost() {
        return connectionHost;
    }

    public void setConnectionHost(String connectionHost) {
        this.connectionHost = connectionHost;
    }

    public long getLatestHeartbeat() {
        return latestHeartbeat;
    }

    public void setLatestHeartbeat(long latestHeartbeat) {
        this.latestHeartbeat = latestHeartbeat;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public List<Short> getAssignmentList() {
        return assignmentList;
    }

    public void setAssignmentList(List<Short> assignmentList) {
        this.assignmentList = assignmentList;
    }
}