package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Payload;

/**
 * TxFeedbackAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/8
 */
public class TxFeedbackAck extends Joyqueue0Payload {

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
        return Joyqueue0CommandType.TX_FEEDBACK_ACK.getCode();
    }
}