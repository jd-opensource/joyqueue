package com.jd.journalq.client.internal.consumer.domain;

import com.jd.journalq.common.exception.JMQCode;

import java.util.Collections;
import java.util.List;

/**
 * FetchMessageData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchMessageData {

    private List<ConsumeMessage> messages;
    private JMQCode code;

    public FetchMessageData() {
    }

    public FetchMessageData(JMQCode code) {
        this.messages = Collections.emptyList();
        this.code = code;
    }

    public FetchMessageData(List<ConsumeMessage> messages, JMQCode code) {
        this.messages = messages;
        this.code = code;
    }

    public List<ConsumeMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ConsumeMessage> messages) {
        this.messages = messages;
    }

    public JMQCode getCode() {
        return code;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }
}