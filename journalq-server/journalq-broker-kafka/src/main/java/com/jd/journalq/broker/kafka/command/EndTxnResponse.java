package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * EndTxnResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class EndTxnResponse extends KafkaRequestOrResponse {

    private short code;

    public EndTxnResponse() {

    }

    public EndTxnResponse(short code) {
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
        return KafkaCommandType.END_TXN.getCode();
    }
}
