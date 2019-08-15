package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * AddProducerRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class AddProducerRequest extends JoyQueuePayload {

    private List<String> topics;
    private String app;
    private long sequence;

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_PRODUCER_REQUEST.getCode();
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public long getSequence() {
        return sequence;
    }
}