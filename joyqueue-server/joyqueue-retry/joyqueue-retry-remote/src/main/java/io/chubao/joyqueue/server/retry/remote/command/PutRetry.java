package io.chubao.joyqueue.server.retry.remote.command;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
import io.chubao.joyqueue.server.retry.model.RetryMessageModel;

import java.util.List;

/**
 * 创建重试数据
 * <p>
 * Created by chengzhiliang on 2019/2/14.
 */
public class PutRetry extends JoyQueuePayload {

    // 重试消息
    private List<RetryMessageModel> messages;

    public PutRetry(List<RetryMessageModel> messages) {
        this.messages = messages;
    }

    @Override
    public int type() {
        return CommandType.PUT_RETRY;
    }

    public List<RetryMessageModel> getMessages() {
        return messages;
    }

    public void setMessages(List<RetryMessageModel> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "PutRetry{" +
                "messages=" + messages +
                '}';
    }

}