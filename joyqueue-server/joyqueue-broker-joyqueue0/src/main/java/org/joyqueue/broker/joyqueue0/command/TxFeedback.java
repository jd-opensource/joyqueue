package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Payload;
import org.joyqueue.network.session.ProducerId;

/**
 * TxFeedback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/8
 */
public class TxFeedback extends Joyqueue0Payload {

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
        return Joyqueue0CommandType.TX_FEEDBACK.getCode();
    }
}