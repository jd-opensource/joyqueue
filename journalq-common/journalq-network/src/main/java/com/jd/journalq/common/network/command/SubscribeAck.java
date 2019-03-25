package com.jd.journalq.common.network.command;

import com.jd.journalq.common.domain.TopicConfig;
import com.jd.journalq.common.network.transport.command.JMQPayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
public class SubscribeAck extends JMQPayload {
    private List<TopicConfig> topicConfigs;

    public SubscribeAck topicConfigs(List<TopicConfig> topicConfigs) {
        this.topicConfigs = topicConfigs;
        return this;
    }

    @Override
    public int type() {
        return JMQCommandType.MQTT_SUBSCRIBE_ACK.getCode();
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
