package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * EndTxnRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class EndTxnRequest extends KafkaRequestOrResponse {

    private String transactionId;
    private long producerId;
    private short producerEpoch;

    // false = ABORT, true = COMMIT
    private boolean transactionResult;

    public boolean isCommit() {
        return transactionResult;
    }

    public boolean isAbort() {
        return !transactionResult;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public boolean isTransactionResult() {
        return transactionResult;
    }

    public void setTransactionResult(boolean transactionResult) {
        this.transactionResult = transactionResult;
    }

    @Override
    public int type() {
        return KafkaCommandType.END_TXN.getCode();
    }
}
