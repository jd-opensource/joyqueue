package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import org.joyqueue.network.session.ProducerId;

/**
 * TxFeedback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/8
 */
public class TxFeedback extends JMQ2Payload {

    private String app;
    private ProducerId producerId;
    private int longPull = 0;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public ProducerId getProducerId() {
        return producerId;
    }

    public void setProducerId(ProducerId producerId) {
        this.producerId = producerId;
    }

    public int getLongPull() {
        return longPull;
    }

    public void setLongPull(int longPull) {
        this.longPull = longPull;
    }

    @Override
    public int type() {
        return JMQ2CommandType.TX_FEEDBACK.getCode();
    }
}