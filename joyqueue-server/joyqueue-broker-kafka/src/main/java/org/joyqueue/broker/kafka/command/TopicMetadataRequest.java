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

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-27.
 */
public class TopicMetadataRequest extends KafkaRequestOrResponse {

    private boolean allowAutoTopicCreation;
    private List<String> topics;

    public boolean isAllowAutoTopicCreation() {
        return allowAutoTopicCreation;
    }

    public void setAllowAutoTopicCreation(boolean allowAutoTopicCreation) {
        this.allowAutoTopicCreation = allowAutoTopicCreation;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getTopics() {
        return topics;
    }

    @Override
    public int type() {
        return KafkaCommandType.METADATA.getCode();
    }

    @Override
    public String toString() {
        StringBuilder topicMetadataRequest = new StringBuilder();
        topicMetadataRequest.append("Name: " + this.getClass().getSimpleName());
        if (topics != null && !topics.isEmpty()) {
            for (String topic : topics) {
                topicMetadataRequest.append("; Topic: " + topic);
            }
        }
        return topicMetadataRequest.toString();
    }
}
