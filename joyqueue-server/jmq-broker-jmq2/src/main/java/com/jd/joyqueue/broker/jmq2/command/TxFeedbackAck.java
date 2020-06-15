package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;

/**
 * TxFeedbackAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/8
 */
public class TxFeedbackAck extends JMQ2Payload {

    private String queryId;
    private long txStartTime;

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public long getTxStartTime() {
        return txStartTime;
    }

    public void setTxStartTime(long txStartTime) {
        this.txStartTime = txStartTime;
    }

    @Override
    public int type() {
        return JMQ2CommandType.TX_FEEDBACK_ACK.getCode();
    }
}