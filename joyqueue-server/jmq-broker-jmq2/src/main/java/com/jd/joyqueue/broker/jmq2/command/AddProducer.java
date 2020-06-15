package com.jd.joyqueue.broker.jmq2.command;

import com.google.common.base.Preconditions;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.session.ProducerId;

/**
 * 添加生产者
 */
public class AddProducer extends JMQ2Payload {
    // 生产者ID
    private ProducerId producerId;
    // 主题
    private TopicName topic;

    public AddProducer topic(final TopicName topic) {
        setTopic(topic);
        return this;
    }

    public AddProducer producerId(final ProducerId producerId) {
        setProducerId(producerId);
        return this;
    }

    public ProducerId getProducerId() {
        return this.producerId;
    }

    public void setProducerId(ProducerId producerId) {
        this.producerId = producerId;
    }

    public TopicName getTopic() {
        return this.topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(producerId != null, "producer ID can not be null");
        Preconditions.checkArgument(topic != null, "topic can not be null");
    }

    @Override
    public int type() {
        return JMQ2CommandType.ADD_PRODUCER.getCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddProducer{");
        sb.append("producerId=").append(producerId);
        sb.append(", topic='").append(topic).append('\'');
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

        AddProducer that = (AddProducer) o;

        if (producerId != null ? !producerId.equals(that.producerId) : that.producerId != null) {
            return false;
        }
        if (topic != null ? !topic.equals(that.topic) : that.topic != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = producerId != null ? producerId.hashCode() : 0;
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        return result;
    }
}