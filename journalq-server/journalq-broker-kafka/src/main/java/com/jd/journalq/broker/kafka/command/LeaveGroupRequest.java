package com.jd.journalq.broker.kafka.command;


import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * Created by zhangkepeng on 17-2-10.
 */
public class LeaveGroupRequest extends KafkaRequestOrResponse {
    private String groupId;
    private String memberId;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public int type() {
        return KafkaCommandType.LEAVE_GROUP.getCode();
    }

    @Override
    public String toString() {
        StringBuilder requestStringBuilder = new StringBuilder();
        requestStringBuilder.append("Name: " + this.getClass().getSimpleName());
        return requestStringBuilder.toString();
    }
}