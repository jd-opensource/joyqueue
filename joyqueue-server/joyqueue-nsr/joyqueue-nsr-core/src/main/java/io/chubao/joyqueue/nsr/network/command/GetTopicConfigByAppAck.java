package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigByAppAck extends JoyQueuePayload {
    private Map<TopicName, TopicConfig> topicConfigs;

    public GetTopicConfigByAppAck topicConfigs(Map<TopicName, TopicConfig> topicConfigs){
        this.topicConfigs = topicConfigs;
        return this;
    }

    public Map<TopicName, TopicConfig> getTopicConfigs() {
        return topicConfigs;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIGS_BY_APP_ACK;
    }
}
