package com.jd.journalq.server.retry.remote.command;

import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.JMQPayload;
import com.jd.journalq.server.retry.model.RetryMessageModel;

import java.util.List;

/**
 * 重试应答
 * <p>
 * Created by chengzhiliang on 2019/2/14.
 */
public class GetRetryAck extends JMQPayload {

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
