package com.jd.journalq.common.network.command;

import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.message.BrokerMessage;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * FetchTopicMessageAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchTopicMessageAckData {

    private List<BrokerMessage> messages;
    private List<ByteBuffer> buffers;
    private JMQCode code;

    public FetchTopicMessageAckData() {

    }

    public FetchTopicMessageAckData(JMQCode code) {
        this.code = code;
        this.buffers = Collections.emptyList();
    }

    public FetchTopicMessageAckData(List<BrokerMessage> messages, JMQCode code) {
        this.messages = messages;
        this.code = code;
    }

    public List<BrokerMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BrokerMessage> messages) {
        this.messages = messages;
    }

    public List<ByteBuffer> getBuffers() {
        return buffers;
    }

    public void setBuffers(List<ByteBuffer> buffers) {
        this.buffers = buffers;
    }

    public JMQCode getCode() {
        return code;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }
}