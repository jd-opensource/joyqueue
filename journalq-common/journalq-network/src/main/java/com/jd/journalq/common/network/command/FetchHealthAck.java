package com.jd.journalq.common.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * FetchHealthAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthAck extends JMQPayload {

    private double point;

    public FetchHealthAck() {

    }

    public FetchHealthAck(double point) {
        this.point = point;
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_HEALTH_ACK.getCode();
    }

    public void setPoint(double point) {
        this.point = point;
    }

    public double getPoint() {
        return point;
    }
}