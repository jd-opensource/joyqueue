package com.jd.journalq.client.internal.producer.domain;

import com.jd.journalq.common.exception.JMQCode;

import java.io.Serializable;

/**
 * SendPrepareResult
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class SendPrepareResult implements Serializable {

    private String txId;
    private JMQCode code;

    public SendPrepareResult() {

    }

    public SendPrepareResult(String txId, JMQCode code) {
        this.txId = txId;
        this.code = code;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public JMQCode getCode() {
        return code;
    }
}