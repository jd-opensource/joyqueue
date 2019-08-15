package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * FetchTopicMessageRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageRequest extends JoyQueuePayload {

    private Map<String, FetchTopicMessageData> topics;
    private String app;
    private int ackTimeout;
    private int longPollTimeout;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_TOPIC_MESSAGE_REQUEST.getCode();
    }

    public void setTopics(Map<String, FetchTopicMessageData> topics) {
        this.topics = topics;
    }

    public Map<String, FetchTopicMessageData> getTopics() {
        return topics;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public int getLongPollTimeout() {
        return longPollTimeout;
    }

    public void setLongPollTimeout(int longPollTimeout) {
        this.longPollTimeout = longPollTimeout;
    }
}
