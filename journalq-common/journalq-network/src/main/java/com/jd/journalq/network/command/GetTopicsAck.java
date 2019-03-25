package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.Payload;
import com.jd.journalq.network.transport.command.Types;

import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
public class GetTopicsAck implements Payload, Types {
    private Set<String> topics;

    public GetTopicsAck topics(Set<String> topics){
        this.topics = topics;
        return this;
    }
    public Set<String> getTopics() {
        return topics;
    }

    @Override
    public int[] types() {
        return new int[]{CommandType.GET_TOPICS_ACK,JMQCommandType.MQTT_GET_TOPICS_ACK.getCode()};
    }
}
