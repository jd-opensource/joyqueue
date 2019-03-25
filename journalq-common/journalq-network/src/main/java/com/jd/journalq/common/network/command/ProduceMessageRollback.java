package com.jd.journalq.common.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * ProduceMessageRollback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageRollback extends JMQPayload {

    private String topic;
    private String app;
    private String txId;

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_ROLLBACK.getCode();
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }
}