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
package org.joyqueue.broker.replication;

import org.joyqueue.broker.election.TopicPartitionGroup;


/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/26
 */
public class Replica {
    private TopicPartitionGroup topicPartitionGroup;

    // replica id
    private int replicaId = 0;

    // replica address
    private String address;

    // write position of this replica
    private long writePosition = 0;

    // commit position of this replica
    // leader replica send commit position to follower replica
    private long commitPosition = 0;

    // next position which leader will send to replica
    private long nextPosition = 0;

    // if the log of this replica match with with leader
    private boolean match = false;

    private long lastAppendSuccessTime;

    private long lastReplicateConsumePosTime;

    private ReplicateCommandState replicateCommandState;

    private long lastAppendTime;

    Replica(int replicaId, String address) {
        this.replicaId = replicaId;
        this.address = address;
    }

    public TopicPartitionGroup getTopicPartitionGroup() {
        return topicPartitionGroup;
    }

    public void setTopicPartitionGroup(TopicPartitionGroup topicPartitionGroup) {
        this.topicPartitionGroup = topicPartitionGroup;
    }

    public int replicaId() {
        return replicaId;
    }

    public void replicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    public String getAddress() {
        return address;
    }

    public String getIp() {
        return address.split(":")[0];
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long writePosition() {
        return writePosition;
    }

    void writePosition(long writePosition) {
        this.writePosition = writePosition;
    }

    public long commitPosition() {
        return commitPosition;
    }

    public void commitPosition(long commitPosition) {
        this.commitPosition = commitPosition;
    }

    public long nextPosition() {
        return nextPosition;
    }

    void nextPosition(long nextPosition) {
        this.nextPosition = nextPosition;
    }


    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    void lastReplicateConsumePosTime(long lastReplicateConsumePosTime) {
        this.lastReplicateConsumePosTime = lastReplicateConsumePosTime;
    }

    public long lastReplicateConsumePosTime() {
        return lastReplicateConsumePosTime;
    }

    void lastAppendSuccessTime(long lastAppendSuccessTime) {
        this.lastAppendSuccessTime = lastAppendSuccessTime;
    }

    public long lastAppendSuccessTime() {
        return lastAppendSuccessTime;
    }

    public ReplicateCommandState getReplicateCommandState() {
        return replicateCommandState;
    }

    public void setReplicateCommandState(ReplicateCommandState replicateCommandState) {
        this.replicateCommandState = replicateCommandState;
    }

    public long getLastAppendTime() {
        return lastAppendTime;
    }

    public void setLastAppendTime(long lastAppendTime) {
        this.lastAppendTime = lastAppendTime;
    }

    @Override
    public String toString() {
        return new StringBuilder("Replica:{").append("replicaId:").append(replicaId)
                .append(", address:").append(address)
                .append(", writePosition:").append(writePosition)
                .append(", commitPosition:").append(commitPosition)
                .append(", nextPosition:").append(nextPosition)
                .append(", match:").append(match)
                .append(", lastAppendSuccessTime:").append(lastAppendSuccessTime)
                .append(", lastReplicateConsumePosTime:").append(lastReplicateConsumePosTime).toString();

    }

    enum ReplicateCommandState {
        REPLICATE,
        WAITING_RESPONSE
    }

}
