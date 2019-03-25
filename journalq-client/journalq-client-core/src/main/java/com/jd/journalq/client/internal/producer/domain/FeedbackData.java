package com.jd.journalq.client.internal.producer.domain;

/**
 * FeedbackData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/24
 */
public class FeedbackData {

    private String topic;
    private String txId;
    private String transactionId;

    public FeedbackData() {

    }

    public FeedbackData(String topic, String txId, String transactionId) {
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

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}