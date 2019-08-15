package io.chubao.joyqueue.client.internal.consumer.domain;

import io.chubao.joyqueue.exception.JoyQueueCode;

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
    private JoyQueueCode code;

    public FetchMessageData() {
    }

    public FetchMessageData(JoyQueueCode code) {
        this.messages = Collections.emptyList();
        this.code = code;
    }

    public FetchMessageData(List<ConsumeMessage> messages, JoyQueueCode code) {
        this.messages = messages;
        this.code = code;
    }

    public List<ConsumeMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ConsumeMessage> messages) {
        this.messages = messages;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }
}