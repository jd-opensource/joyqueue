package com.jd.journalq.broker.mqtt.subscriptions;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.io.Serializable;

/**
 * @author majun8
 */
public class MqttSubscription implements Serializable {
    private static final long serialVersionUID = -1L;

    private final MqttQoS requestedQos;
    private final String clientId;
    private final TopicFilter topicFilter;

    public MqttSubscription(String clientId, TopicFilter topicFilter, MqttQoS requestedQos) {
        this.clientId = clientId;
        this.topicFilter = topicFilter;
        this.requestedQos = requestedQos;
    }

    public MqttSubscription(MqttSubscription source) {
        this.clientId = source.clientId;
        this.topicFilter = source.topicFilter;
        this.requestedQos = source.requestedQos;
    }

    public MqttQoS getRequestedQos() {
        return requestedQos;
    }

    public String getClientId() {
        return clientId;
    }

    public TopicFilter getTopicFilter() {
        return topicFilter;
    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (topicFilter != null ? topicFilter.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MqttSubscription that = (MqttSubscription) o;

        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null)
            return false;
        return !(topicFilter != null ? !topicFilter.equals(that.topicFilter) : that.topicFilter != null);
    }

    @Override
    public String toString() {
        return "MqttSubscription{" +
                "requestedQos=" + requestedQos +
                ", clientId='" + clientId + '\'' +
                ", topicFilter=" + topicFilter +
                '}';
    }
}
