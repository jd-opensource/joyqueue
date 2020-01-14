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
import java.util.Map;

/**
 * Created by zhangkepeng on 16-7-28.
 */
public class OffsetFetchRequest extends KafkaRequestOrResponse {
    private String groupId;
    private Map<String, List<Integer>> topicAndPartitions;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setTopicAndPartitions(Map<String, List<Integer>> topicAndPartitions) {
        this.topicAndPartitions = topicAndPartitions;
    }

    public Map<String, List<Integer>> getTopicAndPartitions() {
        return topicAndPartitions;
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_FETCH.getCode();
    }

    @Override
    public String toString() {
        return "OffsetFetchRequest{" +
                "groupId='" + groupId + '\'' +
                ", topicAndPartitions=" + topicAndPartitions +
                '}';
    }
}
