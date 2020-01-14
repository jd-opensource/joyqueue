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
package org.joyqueue.broker.election.command;

import org.joyqueue.broker.election.TopicPartitionGroup;
import org.joyqueue.network.transport.command.JoyQueuePayload;
import org.joyqueue.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class AppendEntriesResponse extends JoyQueuePayload {
    private TopicPartitionGroup topicPartitionGroup;
    private int term;

    // If the log of the follower not match leader return false
    // otherwise return true
    private boolean success;

    // Next position that leader should replicate messages to this replica
    private long nextPosition;

    // Write position of the replica
    private long writePosition;

    private int replicaId;

    private int entriesTerm;

    public TopicPartitionGroup getTopicPartitionGroup() {
        return topicPartitionGroup;
    }

    public void setTopicPartitionGroup(TopicPartitionGroup topicPartitionGroup) {
        this.topicPartitionGroup = topicPartitionGroup;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public long getWritePosition() {
        return writePosition;
    }

    public void setWritePosition(long writePosition) {
        this.writePosition = writePosition;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(long nextPosition) {
        this.nextPosition = nextPosition;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    public int getEntriesTerm() {
        return entriesTerm;
    }

    public void setEntriesTerm(int entriesTerm) {
        this.entriesTerm = entriesTerm;
    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_RESPONSE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("appendEntriesResponse:{")
                .append("term:").append(term)
                .append(", success:").append(success)
                .append(", nextPosition:").append(nextPosition)
                .append(", writePosition:").append(writePosition)
                .append(", replicaId:").append(replicaId)
                .append(", entriesTerm:").append(entriesTerm)
                .append("}");
        return sb.toString();
    }

    public static class Build{
        private AppendEntriesResponse appendEntriesResponse = new AppendEntriesResponse();

        public static Build create() {
            return new Build();
        }

        public AppendEntriesResponse build() {
            return appendEntriesResponse;
        }

        public Build term(int term) {
            appendEntriesResponse.setTerm(term);
            return this;
        }

        public Build writePosition(long writePosition) {
            appendEntriesResponse.setWritePosition(writePosition);
            return this;
        }

        public Build success(boolean success) {
            appendEntriesResponse.setSuccess(success);
            return this;
        }

        public Build nextPosition(long nextPosition) {
            appendEntriesResponse.setNextPosition(nextPosition);
            return this;
        }

        public Build replicaId(int replicaId) {
            appendEntriesResponse.setReplicaId(replicaId);
            return this;
        }

        public Build entriesTerm(int entriesTerm) {
            appendEntriesResponse.setEntriesTerm(entriesTerm);
            return this;
        }

        public Build topicPartitionGroup(TopicPartitionGroup topicPartitionGroup) {
            appendEntriesResponse.setTopicPartitionGroup(topicPartitionGroup);
            return this;
        }
    }
}
