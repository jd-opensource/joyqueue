package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

import java.util.Map;

/**
 * Created by zhangkepeng on 17-2-10.
 */
public class SyncGroupRequest extends KafkaRequestOrResponse {

    private String groupId;
    private int generationId;
    private String memberId;
    private Map<String, SyncGroupAssignment> groupAssignment;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getGenerationId() {
        return generationId;
    }

    public void setGenerationId(int generationId) {
        this.generationId = generationId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setGroupAssignment(Map<String, SyncGroupAssignment> groupAssignment) {
        this.groupAssignment = groupAssignment;
    }

    public Map<String, SyncGroupAssignment> getGroupAssignment() {
        return groupAssignment;
    }

    @Override
    public int type() {
        return KafkaCommandType.SYNC_GROUP.getCode();
    }

    @Override
    public String toString() {
        StringBuilder requestStringBuilder = new StringBuilder();
        requestStringBuilder.append("Name: " + this.getClass().getSimpleName());
        return requestStringBuilder.toString();
    }
}
