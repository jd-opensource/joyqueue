package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Payload;

/**
 * @author majun8
 */
public class ResetConsumeOffset extends Joyqueue0Payload {
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
        return Joyqueue0CommandType.RESET_CONSUMER_OFFSET.getCode();
    }
}
