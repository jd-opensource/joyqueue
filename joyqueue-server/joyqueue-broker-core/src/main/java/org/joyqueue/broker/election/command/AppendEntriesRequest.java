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
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.JoyQueuePayload;
import org.joyqueue.network.transport.command.Releasable;

import java.nio.ByteBuffer;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class AppendEntriesRequest extends JoyQueuePayload implements Releasable {
    private TopicPartitionGroup topicPartitionGroup;

    private int term;
    private int leaderId;

    // position of previous message
    private long prevPosition;
    // term of previous message
    private int prevTerm;

    // the start position of the entries
    private long startPosition;
    // commit position of the raft cluster
    private long commitPosition;
    // left position of the leader
    private long leftPosition;

    private boolean match;

    private int entriesTerm;

    private ByteBuffer entries;

    public TopicPartitionGroup getTopicPartitionGroup() {
        return topicPartitionGroup;
    }

    public void setTopicPartitionGroup(TopicPartitionGroup topicPartitionGroup) {
        this.topicPartitionGroup = topicPartitionGroup;
    }

    public String getTopic() {
        return topicPartitionGroup.getTopic();
    }

    public int getPartitionGroup() {
        return topicPartitionGroup.getPartitionGroupId();
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public long getPrevPosition() {
        return prevPosition;
    }

    public void setPrevPosition(long prevPosition) {
        this.prevPosition = prevPosition;
    }

    public int getPrevTerm() {
        return prevTerm;
    }

    public void setPrevTerm(int prevTerm) {
        this.prevTerm = prevTerm;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getCommitPosition() {
        return commitPosition;
    }

    public void setCommitPosition(long commitPosition) {
        this.commitPosition = commitPosition;
    }

    public long getLeftPosition() {
        return leftPosition;
    }

    public void setLeftPosition(long leftPosition) {
        this.leftPosition = leftPosition;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public int getEntriesTerm() {
        return entriesTerm;
    }

    public void setEntriesTerm(int entriesTerm) {
        this.entriesTerm = entriesTerm;
    }

    public ByteBuffer getEntries() {
        return entries;
    }

    public void setEntries(ByteBuffer entries) {
        this.entries = entries;
    }

    public int getEntriesLength() {
        if (entries == null) {
            return 0;
        }
        return entries.remaining();
    }

    @Override
    public int type() {
        return CommandType.RAFT_APPEND_ENTRIES_REQUEST;
    }


    @Override
    public String toString() {
        return new StringBuilder("appendEntiresRequest:{")
                .append("topic:").append(getTopic())
                .append(", partitionGroup:").append(getPartitionGroup())
                .append(", term:").append(term)
                .append(", leaderId:").append(leaderId)
                .append(", prevTerm:").append(prevTerm)
                .append(", prevPosition:").append(prevPosition)
                .append(", startPosition:").append(startPosition)
                .append(", commitPosition:").append(commitPosition)
                .append(", leftPosition:").append(leftPosition)
                .append(", match:").append(match)
                .append(", entriesTerm:").append(entriesTerm)
                .append(", entryLength:").append(entries == null ? 0 : entries.remaining())
                .append("}").toString();
    }

    @Override
    public void release() {
        if (entries != null) {
            entries = null;
        }
    }

    public static class Build {
        private AppendEntriesRequest appendEntriesRequest = new AppendEntriesRequest();

        public static Build create() {
            return new Build();
        }

        public AppendEntriesRequest build() {
            return appendEntriesRequest;
        }

        public Build partitionGroup(TopicPartitionGroup partitionGroup) {
            appendEntriesRequest.setTopicPartitionGroup(partitionGroup);
            return this;
        }

        public Build term(int term) {
            appendEntriesRequest.setTerm(term);
            return this;
        }

        public Build leader(int leader) {
            appendEntriesRequest.setLeaderId(leader);
            return this;
        }

        public Build commitPosition(long commitPosition) {
            appendEntriesRequest.setCommitPosition(commitPosition);
            return this;
        }

        public Build startPosition(long startPosition) {
            appendEntriesRequest.setStartPosition(startPosition);
            return this;
        }

        public Build leftPosition(long leftPosition) {
            appendEntriesRequest.setLeftPosition(leftPosition);
            return this;
        }

        public Build match(boolean match) {
            appendEntriesRequest.setMatch(match);
            return this;
        }

        public Build prevTerm(int prevTerm) {
            appendEntriesRequest.setPrevTerm(prevTerm);
            return this;
        }

        public Build prevPosition(long prevPosition) {
            appendEntriesRequest.setPrevPosition(prevPosition);
            return this;
        }

        public Build entriesTerm(int entriesTerm) {
            appendEntriesRequest.setEntriesTerm(entriesTerm);
            return this;
        }

        public Build entries(ByteBuffer entries) {
            appendEntriesRequest.setEntries(entries);
            return this;
        }
    }
}
