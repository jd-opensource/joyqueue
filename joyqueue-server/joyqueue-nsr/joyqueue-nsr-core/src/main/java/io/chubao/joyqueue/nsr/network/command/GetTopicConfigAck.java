package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigAck extends JoyQueuePayload {
    private TopicConfig topicConfig;
    public GetTopicConfigAck topicConfig(TopicConfig topicConfig){
        this.topicConfig = topicConfig;
        return this;
    }

    public TopicConfig getTopicConfig() {
        return topicConfig;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIG_ACK;
    }
}
