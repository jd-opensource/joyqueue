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
public class VoteRequest extends JoyQueuePayload {
    private TopicPartitionGroup topicPartitionGroup;
    private int term;
    private int candidateId;
    private long lastLogPos;
    private int lastLogTerm;
    private boolean preVote;

    public VoteRequest(){}

    public VoteRequest(TopicPartitionGroup topicPartitionGroup, int term,
                       int candidateId, int lastLogTerm, long lastLogPos, boolean preVote) {
        this.topicPartitionGroup = topicPartitionGroup;
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogTerm = lastLogTerm;
        this.lastLogPos = lastLogPos;
        this.preVote = preVote;
    }

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

    public long getLastLogPos() {
        return lastLogPos;
    }

    public void setLastLogPos(long lastLogPos) {
        this.lastLogPos = lastLogPos;
    }

    public int getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(int lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public boolean isPreVote() {
        return preVote;
    }

    public void setPreVote(boolean preVote) {
        this.preVote = preVote;
    }

    @Override
    public int type() {
        return CommandType.RAFT_VOTE_REQUEST;
    }

    @Override
    public String toString() {
        return "VoteRequest{" +
                "topicPartitionGroup=" + topicPartitionGroup +
                ", term=" + term +
                ", candidateId=" + candidateId +
                ", lastLogPos=" + lastLogPos +
                ", lastLogTerm=" + lastLogTerm +
                ", preVote=" + preVote +
                '}';
    }
}
