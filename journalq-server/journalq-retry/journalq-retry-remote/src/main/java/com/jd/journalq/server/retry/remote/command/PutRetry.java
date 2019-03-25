package com.jd.journalq.server.retry.remote.command;

import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.command.JMQPayload;
import com.jd.journalq.server.retry.model.RetryMessageModel;

import java.util.List;

/**
 * 创建重试数据
 * <p>
 * Created by chengzhiliang on 2019/2/14.
 */
public class PutRetry extends JMQPayload {

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
}