package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
public class SubscribeAck extends JoyQueuePayload {
    private List<TopicConfig> topicConfigs;

    public SubscribeAck topicConfigs(List<TopicConfig> topicConfigs) {
        this.topicConfigs = topicConfigs;
        return this;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.MQTT_SUBSCRIBE_ACK.getCode();
    }


    public List<TopicConfig> getTopicConfigs() {
        return topicConfigs;
    }

    public void setTopicConfigs(List<TopicConfig> topicConfigs) {
        this.topicConfigs = topicConfigs;
    }

    @Override
    public String toString() {
        return "SubscribeAck{" +
                "topicConfigs=" + topicConfigs +
                '}';
    }
}
