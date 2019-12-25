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


import org.joyqueue.network.transport.command.JoyQueuePayload;
import org.joyqueue.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class VoteResponse extends JoyQueuePayload {
    private int term;
    private int candidateId;
    private int voteNodeId;
    private boolean voteGranted;

    public VoteResponse(){}

    public VoteResponse(int term, int candidateId, int voteNodeId, boolean voteGranted) {
        this.term = term;
        this.candidateId = candidateId;
        this.voteNodeId = voteNodeId;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getVoteNodeId() {
        return voteNodeId;
    }

    public void setVoteNodeId(int voteNodeId) {
        this.voteNodeId = voteNodeId;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    @Override
    public int type() {
        return CommandType.RAFT_VOTE_RESPONSE;
    }
}
