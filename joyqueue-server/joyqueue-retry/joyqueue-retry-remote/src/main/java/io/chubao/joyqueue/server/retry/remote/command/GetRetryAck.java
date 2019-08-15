package io.chubao.joyqueue.server.retry.remote.command;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
import io.chubao.joyqueue.server.retry.model.RetryMessageModel;

import java.util.List;

/**
 * 重试应答
 * <p>
 * Created by chengzhiliang on 2019/2/14.
 */
public class GetRetryAck extends JoyQueuePayload {

    // 存储的消息
    protected List<RetryMessageModel> messages;

    @Override
    public int type() {
        return CommandType.GET_RETRY_ACK;
    }

    public GetRetryAck() {
    }

    public List<RetryMessageModel> getMessages() {
        return messages;
    }

    public void setMessages(List<RetryMessageModel> messages) {
        this.messages = messages;
    }

}
