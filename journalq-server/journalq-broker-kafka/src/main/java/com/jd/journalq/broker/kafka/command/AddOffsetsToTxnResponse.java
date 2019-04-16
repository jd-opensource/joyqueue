package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * AddOffsetsToTxnResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class AddOffsetsToTxnResponse extends KafkaRequestOrResponse {

    private short code;

    public AddOffsetsToTxnResponse() {

    }

    public AddOffsetsToTxnResponse(short code) {
        this.code = code;
    }

    public void setCode(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_OFFSETS_TO_TXN.getCode();
    }
}
