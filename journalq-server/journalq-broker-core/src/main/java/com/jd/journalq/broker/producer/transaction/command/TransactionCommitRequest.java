package com.jd.journalq.broker.producer.transaction.command;

import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.JournalqPayload;

import java.util.List;

/**
 * TransactionCommitRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionCommitRequest extends JournalqPayload {

    private String topic;
    private String app;
    private List<String> txIds;

    public TransactionCommitRequest() {

    }

    public TransactionCommitRequest(String topic, String app, List<String> txIds) {
        this.topic = topic;
        this.app = app;
        this.txIds = txIds;
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

    public void setTxIds(List<String> txIds) {
        this.txIds = txIds;
    }

    public List<String> getTxIds() {
        return txIds;
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_COMMIT_REQUEST;
    }

    @Override
    public String toString() {
        return "TransactionCommitRequest{" +
                "topic='" + topic + '\'' +
                ", app='" + app + '\'' +
                ", txIds=" + txIds +
                '}';
    }
}