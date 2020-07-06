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
package org.joyqueue.broker.kafka.command;


import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.model.KafkaBroker;
import org.joyqueue.broker.kafka.model.KafkaTopicMetadata;

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-29.
 */
public class TopicMetadataResponse extends KafkaRequestOrResponse {

    private List<KafkaTopicMetadata> topicMetadatas;
    private List<KafkaBroker> brokers;
    private String clusterId;

    public TopicMetadataResponse(List<KafkaTopicMetadata> topicMetadatas, List<KafkaBroker> brokers) {
        this.topicMetadatas = topicMetadatas;
        this.brokers = brokers;
    }

    public TopicMetadataResponse(List<KafkaTopicMetadata> topicMetadatas, List<KafkaBroker> brokers, String clusterId) {
        this.topicMetadatas = topicMetadatas;
        this.brokers = brokers;
        this.clusterId = clusterId;
    }

    public List<KafkaTopicMetadata> getTopicMetadatas() {
        return topicMetadatas;
    }

    public void setTopicMetadatas(List<KafkaTopicMetadata> topicMetadatas) {
        this.topicMetadatas = topicMetadatas;
    }

    public List<KafkaBroker> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<KafkaBroker> brokers) {
        this.brokers = brokers;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }

    @Override
    public int type() {
        return KafkaCommandType.METADATA.getCode();
    }

    @Override
    public String toString() {
        return "TopicMetadataResponse{" +
                "topicMetadatas=" + topicMetadatas +
                ", brokers=" + brokers +
                ", clusterId='" + clusterId + '\'' +
                '}';
    }
}
