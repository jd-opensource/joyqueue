package io.chubao.joyqueue.client.internal.producer.domain;

import io.chubao.joyqueue.exception.JoyQueueCode;

import java.io.Serializable;

/**
 * SendPrepareResult
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class SendPrepareResult implements Serializable {

    private String txId;
    private JoyQueueCode code;

    public SendPrepareResult() {

    }

    public SendPrepareResult(String txId, JoyQueueCode code) {
        this.txId = txId;
        this.code = code;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}