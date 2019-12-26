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
package org.joyqueue.nsr.network.command;

import org.joyqueue.domain.TopicName;
import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class LeaderReport extends JoyQueuePayload {
    private TopicName topic;
    private int partitionGroup;
    private int leaderBrokerId;
    private Set<Integer> isrId;
    private int termId;

    public LeaderReport topic(TopicName topic){
        this.topic = topic;
        return this;
    }
    public LeaderReport partitionGroup(int partitionGroup){
        this.partitionGroup = partitionGroup;
        return this;
    }
    public LeaderReport leaderBrokerId(int leaderBrokerId){
        this.leaderBrokerId = leaderBrokerId;
        return this;
    }
    public LeaderReport isrId(Set<Integer> isrId){
        this.isrId = isrId;
        return this;
    }
    public LeaderReport termId(int termId){
        this.termId = termId;
        return this;
    }

    public TopicName getTopic() {
        return topic;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public int getLeaderBrokerId() {
        return leaderBrokerId;
    }

    public Set<Integer> getIsrId() {
        return isrId;
    }

    public int getTermId() {
        return termId;
    }

    @Override
    public int type() {
        return NsrCommandType.LEADER_REPORT;
    }

    @Override
    public String toString() {
        return "LeaderReport{" +
                "topic=" + topic +
                ", partitionGroup=" + partitionGroup +
                ", leaderBrokerId=" + leaderBrokerId +
                ", isrId=" + isrId +
                ", termId=" + termId +
                '}';
    }
}
