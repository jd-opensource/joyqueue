/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.mqtt.subscriptions;

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
