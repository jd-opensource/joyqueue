package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;

/**
 * @author majun8
 */
public class ResetConsumeOffset extends JMQ2Payload {
    // 主题
    protected String topic;
    // 应用
    protected String app;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    @Override
    public int type() {
        return JMQ2CommandType.RESET_CONSUMER_OFFSET.getCode();
    }
}
