package com.jd.journalq.common.network.command;

/**
 * FetchProduceFeedbackAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class FetchProduceFeedbackAckData {

    private String topic;
    private String txId;
    private String transactionId;

    public FetchProduceFeedbackAckData() {

    }

    public FetchProduceFeedbackAckData(String topic, String txId, String transactionId) {
        this.topic = topic;
        this.txId = txId;
        this.transactionId = transactionId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}