package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.google.common.base.Preconditions;

/**
 * 复制身份信息
 */
public class Identity extends JMQ2Payload {
    // JMQ2Broker
    private JMQ2Broker broker;


    public Identity broker(final JMQ2Broker broker) {
        setBroker(broker);
        return this;
    }

    public JMQ2Broker getBroker() {
        return this.broker;
    }


    public void setBroker(JMQ2Broker broker) {
        this.broker = broker;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(broker!=null, "broker can not be null");
    }

    @Override
    public int type() {
        return JMQ2CommandType.IDENTITY.getCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Identity{");
        sb.append("broker=").append(broker);
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

        Identity identity = (Identity) o;

        if (broker != null ? !broker.equals(identity.broker) : identity.broker != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (broker != null ? broker.hashCode() : 0);
        return result;
    }

}