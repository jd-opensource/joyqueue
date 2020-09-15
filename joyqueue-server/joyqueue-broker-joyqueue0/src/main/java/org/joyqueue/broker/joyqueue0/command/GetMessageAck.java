package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Payload;
import org.joyqueue.broker.network.traffic.FetchResponseTrafficPayload;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.transport.command.Command;

import java.util.List;

/**
 * 请求消息应答
 */
public class GetMessageAck extends Joyqueue0Payload implements FetchResponseTrafficPayload {
    // 存储的消息
    protected List<BrokerMessage> messages;
    protected Traffic traffic;

    public List<BrokerMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BrokerMessage> messages) {
        this.messages = messages;
    }

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }

    /**
     * 构造应答
     *
     * @return 布尔应答
     */
    public static Command build() {
        return build(JoyQueueCode.SUCCESS);
    }

    /**
     * 构造应答
     *
     * @param code
     * @param args
     * @return
     */
    public static Command build(final JoyQueueCode code, Object... args) {
        return build(code.getCode(), code.getMessage(args));
    }

    /**
     * 构造应答
     *
     * @param code 代码
     * @return 布尔应答
     */
    public static Command build(int code, String error) {
        Joyqueue0Header header = new Joyqueue0Header();
        header.setStatus(code);
        header.setError(error);
        header.setType(Joyqueue0CommandType.GET_MESSAGE_ACK.getCode());
        GetMessageAck getMessageAck = new GetMessageAck();
        return new Command(header, getMessageAck);
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_MESSAGE_ACK.getCode();
    }
}