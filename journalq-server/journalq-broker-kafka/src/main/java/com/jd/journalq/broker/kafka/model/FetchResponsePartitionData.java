package com.jd.journalq.broker.kafka.model;

import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.KafkaErrorCode;

import java.util.List;

/**
 * Created by zhangkepeng on 16-8-17.
 *
 */
public class FetchResponsePartitionData {

    private short error = KafkaErrorCode.NONE;
    private long hw = -1L;
    private List<KafkaBrokerMessage> messages;
    private int bytes;

    public FetchResponsePartitionData() {

    }

    public FetchResponsePartitionData(short error) {
        this.error = error;
    }

    public FetchResponsePartitionData(short error, long hw, List<KafkaBrokerMessage> messages) {
        this.error = error;
        this.hw = hw;
        this.messages = messages;
    }

    public short getError() {
        return error;
    }

    public void setError(short error) {
        this.error = error;
    }

    public long getHw() {
        return hw;
    }

    public void setHw(long hw) {
        this.hw = hw;
    }

    public void setMessages(List<KafkaBrokerMessage> messages) {
        this.messages = messages;
    }

    public List<KafkaBrokerMessage> getMessages() {
        return messages;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public int getBytes() {
        return bytes;
    }
}

