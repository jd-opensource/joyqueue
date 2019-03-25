package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.coordinator.CoordinatorType;
import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * Created by zhangkepeng on 17-2-9.
 */
public class FindCoordinatorRequest extends KafkaRequestOrResponse {
    private String groupId;
    private CoordinatorType coordinatorType;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public CoordinatorType getCoordinatorType() {
        return coordinatorType;
    }

    public void setCoordinatorType(CoordinatorType coordinatorType) {
        this.coordinatorType = coordinatorType;
    }

    @Override
    public String toString() {
        StringBuilder requestStringBuilder = new StringBuilder();
        requestStringBuilder.append("Name: " + this.getClass().getSimpleName());
        requestStringBuilder.append("; groupId: " + groupId);
        return requestStringBuilder.toString();
    }

    @Override
    public int type() {
        return KafkaCommandType.FIND_COORDINATOR.getCode();
    }
}
