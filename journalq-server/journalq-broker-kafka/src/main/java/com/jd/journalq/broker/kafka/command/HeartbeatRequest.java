package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * Created by zhangkepeng on 17-2-10.
 */
public class HeartbeatRequest extends KafkaRequestOrResponse {
    private String groupId;
    private int groupGenerationId;
    private String memberId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getGroupGenerationId() {
        return groupGenerationId;
    }

    public void setGroupGenerationId(int groupGenerationId) {
        this.groupGenerationId = groupGenerationId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    @Override
    public int type() {
        return KafkaCommandType.HEARTBEAT.getCode();
    }

    @Override
    public String toString() {
        StringBuilder requestStringBuilder = new StringBuilder();
        requestStringBuilder.append("Name: " + this.getClass().getSimpleName());
        return requestStringBuilder.toString();
    }
}
