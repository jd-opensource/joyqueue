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

import org.joyqueue.domain.Broker;

import java.util.Objects;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/11
 */
public class DefaultElectionNode implements ElectionNode {
    private State state = State.FOLLOWER;
    private long priority;
    private String address;
    private int nodeId;
    private boolean voteGranted = false;

    public DefaultElectionNode() {}

    public DefaultElectionNode(String address, int nodeId) {
        this.address = address;
        this.nodeId = nodeId;
        this.state = State.FOLLOWER;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public long getPriority() {
        return priority;
    }

    @Override
    public void setPriority(long priority) {
        this.priority = priority;
    }

    @Override
    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public boolean isVoteGranted() {
        return voteGranted;
    }

    @Override
    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    @Override
    public boolean equals(ElectionNode node) {
        return nodeId == node.getNodeId();
    }

    public boolean equals(Broker broker) {
        return this.nodeId == broker.getId();
    }

    @Override
    public String toString() {
        return new StringBuilder("DefaultElectionNode:{")
                .append("state:").append(state)
                .append(", address:").append(address)
                .append(", nodeId:").append(nodeId)
                .append(", voteGranted:").append(voteGranted)
                .append("}").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (!(o instanceof DefaultElectionNode)) {
            return false;
        }

        DefaultElectionNode that = (DefaultElectionNode) o;

        return priority == that.priority &&
                nodeId == that.nodeId &&
                voteGranted == that.voteGranted &&
                state == that.state &&
                address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, priority, address, nodeId, voteGranted);
    }
}
