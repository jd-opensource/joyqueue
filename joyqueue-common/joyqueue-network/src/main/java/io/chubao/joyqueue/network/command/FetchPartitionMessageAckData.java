package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.message.BrokerMessage;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * FetchPartitionData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchPartitionMessageAckData {

    private List<BrokerMessage> messages;
    private List<ByteBuffer> buffers;
    private JoyQueueCode code;

    public FetchPartitionMessageAckData() {

    }

    public FetchPartitionMessageAckData(JoyQueueCode code) {
        this.code = code;
        this.buffers = Collections.emptyList();
    }

    public FetchPartitionMessageAckData(List<BrokerMessage> messages, JoyQueueCode code) {
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

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public int getSize() {
        if (buffers == null) {
            return 0;
        }
        int size = 0;
        for (ByteBuffer buffer : buffers) {
            size += buffer.limit();
        }
        return size;
    }
}