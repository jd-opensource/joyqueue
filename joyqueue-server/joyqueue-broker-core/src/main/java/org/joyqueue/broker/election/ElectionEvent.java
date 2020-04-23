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
package org.joyqueue.broker.election;


/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/20
 */
public class ElectionEvent {
    private Type eventType;
    private int term;
    private int leaderId;
    private TopicPartitionGroup topicPartitionGroup;

    public ElectionEvent(Type eventType, int term, int leaderId, TopicPartitionGroup topicPartitionGroup) {
        this.eventType = eventType;
        this.term = term;
        this.leaderId = leaderId;
        this.topicPartitionGroup = topicPartitionGroup;
    }

    public Type getEventType() {
        return eventType;
    }

    public int getTerm() {
        return term;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public TopicPartitionGroup getTopicPartitionGroup() {
        return topicPartitionGroup;
    }

    public enum Type {
        START_ELECTION,// 开始选举
        LEADER_FOUND   // 找到Leader，leader为找到的Leader
    }
}
