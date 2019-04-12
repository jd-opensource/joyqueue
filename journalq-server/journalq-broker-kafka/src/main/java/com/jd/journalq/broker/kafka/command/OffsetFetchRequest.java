/**
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
package com.jd.journalq.broker.kafka.command;


import com.google.common.collect.HashMultimap;
import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * Created by zhangkepeng on 16-7-28.
 */
public class OffsetFetchRequest extends KafkaRequestOrResponse {
    private String groupId;
    private HashMultimap<String, Integer> topicAndPartitions;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public HashMultimap<String, Integer> getTopicAndPartitions() {
        return topicAndPartitions;
    }

    public void setTopicAndPartitions(HashMultimap<String, Integer> topicAndPartitions) {
        this.topicAndPartitions = topicAndPartitions;
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_FETCH.getCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    public String describe() {
        StringBuilder offsetFetchRequest = new StringBuilder();
        offsetFetchRequest.append("Name: " + this.getClass().getSimpleName());
        offsetFetchRequest.append("; Version: " + getVersion());
        offsetFetchRequest.append("; CorrelationId: " + getCorrelationId());
        offsetFetchRequest.append("; ClientId: " + getClientId());
        offsetFetchRequest.append("; GroupId: " + groupId);
        return offsetFetchRequest.toString();
    }
}
