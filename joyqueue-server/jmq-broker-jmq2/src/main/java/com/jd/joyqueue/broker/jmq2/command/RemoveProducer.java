package com.jd.joyqueue.broker.jmq2.command;

import com.google.common.base.Preconditions;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import org.joyqueue.network.session.ProducerId;

public class RemoveProducer extends JMQ2Payload {
    //生产者ID
    private ProducerId producerId;


    public RemoveProducer producerId(final ProducerId producerId) {
        this.producerId = producerId;
        return this;
    }

    public ProducerId getProducerId() {
        return this.producerId;
    }

    public void setProducerId(ProducerId producerId) {
        this.producerId = producerId;
    }


    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(producerId != null, "producer ID can not be null.");
    }

    @Override
    public int type() {
        return JMQ2CommandType.REMOVE_PRODUCER.getCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RemoveProducer{");
        sb.append("producerId=").append(producerId);
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

        RemoveProducer that = (RemoveProducer) o;

        if (producerId != null ? !producerId.equals(that.producerId) : that.producerId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (producerId != null ? producerId.hashCode() : 0);
        return result;
    }
}