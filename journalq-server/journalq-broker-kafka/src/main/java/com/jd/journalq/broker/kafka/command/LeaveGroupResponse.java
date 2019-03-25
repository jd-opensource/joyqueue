package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * Created by zhangkepeng on 17-2-10.
 */
public class LeaveGroupResponse extends KafkaRequestOrResponse {

    private short errorCode;

    public LeaveGroupResponse() {

    }

    public LeaveGroupResponse(short errorCode) {
        this.errorCode = errorCode;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public int type() {
        return KafkaCommandType.LEAVE_GROUP.getCode();
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append("Name: " + this.getClass().getSimpleName());
        return responseStringBuilder.toString();
    }
}