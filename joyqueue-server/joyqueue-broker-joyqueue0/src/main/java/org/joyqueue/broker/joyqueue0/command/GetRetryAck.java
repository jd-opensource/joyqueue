package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Payload;
import org.joyqueue.message.BrokerMessage;

import java.util.Arrays;

/**
 * 重试应答
 *
 * @author Jame.HU
 * @version V1.0
 */
public class GetRetryAck extends Joyqueue0Payload {
    // 存储的消息
    protected BrokerMessage[] messages;

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_RETRY_ACK.getCode();
    }

    public GetRetryAck messages(final BrokerMessage[] messages) {
        this.messages = messages;
        return this;
    }

    public BrokerMessage[] getMessages() {
        return messages;
    }

    public void setMessages(BrokerMessage[] messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GetRetryAckPayload{");
        sb.append("messages=").append(Arrays.toString(messages));
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

        GetRetryAck that = (GetRetryAck) o;

        if (!Arrays.equals(messages, that.messages)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (messages != null ? Arrays.hashCode(messages) : 0);
        return result;
    }
}
