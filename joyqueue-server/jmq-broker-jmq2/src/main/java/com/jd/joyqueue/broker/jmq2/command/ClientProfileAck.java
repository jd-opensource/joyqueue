package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;

/**
 * 客户端性能应答
 */
public class ClientProfileAck extends JMQ2Payload {
    private int interval;

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientProfileAck{");
        sb.append("interval=").append(interval);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ClientProfileAck that = (ClientProfileAck) o;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return result;
    }

    @Override
    public int type() {
        return JMQ2CommandType.CLIENT_PROFILE_ACK.getCode();
    }
}
