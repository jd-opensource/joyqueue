package com.jd.journalq.broker.kafka.coordinator.domain;


import java.util.List;

/**
 * Created by zhuduohui on 2018/5/17.
 */
public class GroupDescribe {

    private short errCode;
    private String groupId;
    private String state;
    private String protocolType;
    private String protocol;
    private List<KafkaCoordinatorGroupMember> members;

    public short getErrCode() {
        return errCode;
    }

    public void setErrCode(short errCode) {
        this.errCode = errCode;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<KafkaCoordinatorGroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<KafkaCoordinatorGroupMember> members) {
        this.members = members;
    }
}

