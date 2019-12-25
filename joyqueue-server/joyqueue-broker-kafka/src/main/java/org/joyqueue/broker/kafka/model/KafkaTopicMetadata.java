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
package org.joyqueue.broker.kafka.model;

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-29.
 */
public class KafkaTopicMetadata {

    private String topic;
    private short errorCode;
    private List<KafkaPartitionMetadata> kafkaPartitionMetadata;

    public KafkaTopicMetadata(String topic, List<KafkaPartitionMetadata> kafkaPartitionMetadata, short errorCode) {
        this.topic = topic;
        this.errorCode = errorCode;
        this.kafkaPartitionMetadata = kafkaPartitionMetadata;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public List<KafkaPartitionMetadata> getKafkaPartitionMetadata() {
        return kafkaPartitionMetadata;
    }

    public void setKafkaPartitionMetadata(List<KafkaPartitionMetadata> kafkaPartitionMetadata) {
        this.kafkaPartitionMetadata = kafkaPartitionMetadata;
    }

    @Override
    public String toString() {
        return "KafkaTopicMetadata{" +
                "topic='" + topic + '\'' +
                ", errorCode=" + errorCode +
                ", kafkaPartitionMetadata=" + kafkaPartitionMetadata +
                '}';
    }
}