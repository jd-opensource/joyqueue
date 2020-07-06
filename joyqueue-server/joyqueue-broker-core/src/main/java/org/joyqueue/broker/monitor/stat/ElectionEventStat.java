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

package org.joyqueue.broker.monitor.stat;


import org.joyqueue.broker.election.ElectionEvent;
import org.joyqueue.broker.election.TopicPartitionGroup;

/**
 *
 *  Election event stat for node(partition group)
 *
 **/
public class ElectionEventStat {
   private TopicPartitionGroup partitionGroup;
   // electing,or done
   private ElectionEvent.Type state;
   // state timestamp
   private long timestamp;
   private int term;

    public ElectionEventStat(){
        this(null,null,-1,0);
    }
   public ElectionEventStat(TopicPartitionGroup partitionGroup, ElectionEvent.Type state, long timeMs, int term){
       this.partitionGroup=partitionGroup;
       this.state=state;
       this.timestamp=timeMs;
       this.term=term;
   }

    public TopicPartitionGroup getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(TopicPartitionGroup partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public ElectionEvent.Type getState() {
        return state;
    }

    public void setState(ElectionEvent.Type state) {
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }
}
