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
package io.chubao.joyqueue.broker.monitor.stat;


import io.chubao.joyqueue.broker.election.ElectionNode;
import io.chubao.joyqueue.broker.election.TopicPartitionGroup;

/**
 *
 * Election(replica) or partition group replica node state and timestamp
 *
 **/
public class ReplicaNodeStat {
    private volatile  ElectionNode.State state;
    private long timestamp;
    private int  brokerId;
    // last interval snapshot value
    private long recentSnapshot;
//    private transient  EnumStateMetric stateMetric=new EnumStateMetric(ElectionNode.State.values().length-1);
    private TopicPartitionGroup partitionGroup;

    public ReplicaNodeStat(int brokerId, TopicPartitionGroup topicPartitionGroup, ElectionNode.State state, long timestamp){
        this.brokerId=brokerId;
        this.partitionGroup=topicPartitionGroup;
        this.state=state;
        this.timestamp=timestamp;
    }

    public ReplicaNodeStat(){
        this(-1,new TopicPartitionGroup(),null,-1);
    }

    public ElectionNode.State getState() {
        return state;
    }

    public void setState(ElectionNode.State state) {
        this.state = state;
//        this.stateMetric.updateState(state.ordinal());
    }

//    /**
//     * Snapshot the interval state value and clear
//     * contain frequency and state sequence
//     **/
//    public long intervalSnapshot(){
//        this.recentSnapshot= stateMetric.snapshotValue();
//             stateMetric.clear();
//        return recentSnapshot;
//    }
//
//    /**
//     * Last interval snapshot value
//     **/
//    public long getRecentSnapshot() {
//        if(recentSnapshot==0)
//          return intervalSnapshot();
//        else return recentSnapshot;
//    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public TopicPartitionGroup getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(TopicPartitionGroup partitionGroup) {
        this.partitionGroup = partitionGroup;
    }


}
