package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.KafkaBroker;

/**
 * Created by zhangkepeng on 17-2-9.
 */
public class FindCoordinatorResponse extends KafkaRequestOrResponse {

    private short errorCode;
    private KafkaBroker broker;

    public FindCoordinatorResponse(short errorCode, KafkaBroker broker) {
        this.errorCode = errorCode;
        this.broker = broker;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public KafkaBroker getBroker() {
        return broker;
    }

    public void setBroker(KafkaBroker broker) {
        this.broker = broker;
    }

    @Override
    public int type() {
        return KafkaCommandType.FIND_COORDINATOR.getCode();
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append("Name: " + this.getClass().getSimpleName());
        return responseStringBuilder.toString();
    }
}
