package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * InitProducerIdResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class InitProducerIdResponse extends KafkaRequestOrResponse {

    private short code;
    private long producerId;
    private short producerEpoch;

    public InitProducerIdResponse() {

    }

    public InitProducerIdResponse(short code, long producerId, short producerEpoch) {
        this.code = code;
        this.producerId = producerId;
        this.producerEpoch = producerEpoch;
    }

    public InitProducerIdResponse(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    public void setCode(short code) {
        this.code = code;
    }

    public long getProducerId() {
        return producerId;
    }

    public void setProducerId(long producerId) {
        this.producerId = producerId;
    }

    public short getProducerEpoch() {
        return producerEpoch;
    }

    public void setProducerEpoch(short producerEpoch) {
        this.producerEpoch = producerEpoch;
    }

    @Override
    public int type() {
        return KafkaCommandType.INIT_PRODUCER_ID.getCode();
    }
}