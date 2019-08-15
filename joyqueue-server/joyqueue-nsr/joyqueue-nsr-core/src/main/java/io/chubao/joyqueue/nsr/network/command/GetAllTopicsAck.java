package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetAllTopicsAck extends JoyQueuePayload {

    private Set<String> topicNames;

    public GetAllTopicsAck topicNames(Set<String> topicNames){
        this.topicNames = topicNames;
        return this;
    }

    public Set<String> getTopicNames() {
        return topicNames;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_ALL_TOPICS_ACK;
    }
}
