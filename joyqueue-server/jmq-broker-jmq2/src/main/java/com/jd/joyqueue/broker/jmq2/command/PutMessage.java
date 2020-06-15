package com.jd.joyqueue.broker.jmq2.command;

import com.google.common.base.Preconditions;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import org.joyqueue.broker.network.traffic.ProduceResponseTrafficPayload;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.session.ProducerId;
import org.joyqueue.network.session.TransactionId;

import java.util.List;

/**
 * 生产消息
 */
public class PutMessage extends JMQ2Payload implements ProduceResponseTrafficPayload {
    // 生产者ID
    private ProducerId producerId;
    // 事务ID
    private TransactionId transactionId;
    // 消息数组
    private List<BrokerMessage> messages;
    // 队列ID
    private short queueId;
    private Traffic traffic;

    public ProducerId getProducerId() {
        return this.producerId;
    }

    public void setProducerId(ProducerId producerId) {
        this.producerId = producerId;
    }

    public PutMessage queueId(final short queueId) {
        setQueueId(queueId);
        return this;
    }

    public PutMessage transactionId(final TransactionId transactionId) {
        setTransactionId(transactionId);
        return this;
    }

    public PutMessage producerId(final ProducerId producerId) {
        setProducerId(producerId);
        return this;
    }

    public TransactionId getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    public List<BrokerMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BrokerMessage> messages) {
        this.messages = messages;
    }

    public short getQueueId() {
        return queueId;
    }

    public void setQueueId(short queueId) {
        this.queueId = queueId;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(producerId != null, "productId must note be null");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PutMessage{");
        sb.append("producerId=").append(producerId);
        sb.append(", transactionId=").append(transactionId);
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

        PutMessage that = (PutMessage) o;

        if (producerId != null ? !producerId.equals(that.producerId) : that.producerId != null) {
            return false;
        }
        if (transactionId != null ? !transactionId.equals(that.transactionId) : that.transactionId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (producerId != null ? producerId.hashCode() : 0);
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        return result;
    }

    @Override
    public int type() {
        return JMQ2CommandType.PUT_MESSAGE.getCode();
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }
}