package com.jd.journalq.broker.producer.transaction.command;

import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * TransactionRollbackRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionRollbackRequest extends JMQPayload {

    private String topic;
    private String app;
    private String txId;

    public TransactionRollbackRequest() {

    }

    public TransactionRollbackRequest(String topic, String app, String txId) {
        this.topic = topic;
        this.app = app;
        this.txId = txId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_ROLLBACK_REQUEST;
    }
}