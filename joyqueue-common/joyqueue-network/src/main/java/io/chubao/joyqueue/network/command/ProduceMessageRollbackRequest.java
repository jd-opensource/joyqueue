package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * ProduceMessageRollbackRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/18
 */
public class ProduceMessageRollbackRequest extends JoyQueuePayload {

    private String topic;
    private String app;
    private String txId;

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_ROLLBACK_REQUEST.getCode();
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