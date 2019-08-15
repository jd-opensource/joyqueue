package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * FetchHealthResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthResponse extends JoyQueuePayload {

    private double point;

    public FetchHealthResponse() {

    }

    public FetchHealthResponse(double point) {
        this.point = point;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_HEALTH_RESPONSE.getCode();
    }

    public void setPoint(double point) {
        this.point = point;
    }

    public double getPoint() {
        return point;
    }
}