package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * AddOffsetsToTxnRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class AddOffsetsToTxnRequest extends KafkaRequestOrResponse {

    private String transactionId;
    private long producerId;
    private short producerEpoch;
    private String groupId;

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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_OFFSETS_TO_TXN.getCode();
    }

    @Override
    public String toString() {
        return "AddOffsetsToTxnRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
