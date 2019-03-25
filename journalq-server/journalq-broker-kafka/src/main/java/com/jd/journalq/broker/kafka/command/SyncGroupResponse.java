package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * Created by zhangkepeng on 17-2-10.
 */
public class SyncGroupResponse extends KafkaRequestOrResponse {

    private short errorCode;
    private SyncGroupAssignment assignment;

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public void setAssignment(SyncGroupAssignment assignment) {
        this.assignment = assignment;
    }

    public SyncGroupAssignment getAssignment() {
        return assignment;
    }

    @Override
    public int type() {
        return KafkaCommandType.SYNC_GROUP.getCode();
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append("Name: " + this.getClass().getSimpleName());
        return responseStringBuilder.toString();
    }
}
