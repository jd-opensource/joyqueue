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
package org.joyqueue.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author lixiaobin6
 * 下午2:39 2018/8/13
 */
public class PartitionGroup implements Serializable {
    /**
     * topic name
     */
    protected TopicName topic;
    /**
     * group index
     */
    protected int group;
    /**
     * partition set
     */
    protected Set<Short> partitions;

    /**
     * leader broker ID
     */
    protected Integer leader = -1;
    /**
     * system recommend leader
     */
    protected Integer recLeader = -1;
    /**
     * elect term
     */
    protected Integer term = 0;

    /**
     * replica(broker) set
     */
    protected Set<Integer> replicas;
    /**
     * in sync replica(broker) ID
     */
    protected Set<Integer> isrs;
    /**
     * learner replica(broker) ID
     */
    protected Set<Integer> learners;
    /**
     * partition group related brokers
     */
    protected Map<Integer, Broker> brokers;
    /**
     * 不需要做数据同步的brokerId
     */
    protected List<Integer> outSyncReplicas = new ArrayList<>();
    /**
     * elect type
     */
    protected ElectType electType = ElectType.fix;

    public String getId() {
        return new StringBuilder(30).append(topic.getFullName()).append(".").append(group).toString();
    }

    public Broker getLeaderBroker() {
        if (leader == null || leader.equals(-1)) {
            return null;
        }
        return null == brokers ? null : brokers.get(leader);
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public Set<Integer> getIsrs() {
        if (null == isrs) {
            return Collections.EMPTY_SET;
        } else {
            return isrs;
        }
    }

    public void setIsrs(Set<Integer> isrs) {
        this.isrs = isrs;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getRecLeader() {
        return recLeader;
    }

    public void setRecLeader(Integer recLeader) {
        this.recLeader = recLeader;
    }

    public Integer getLeader() {
        return leader;
    }

    public void setLeader(Integer leader) {
        this.leader = leader;
    }

    public Set<Short> getPartitions() {
        if (null == partitions) {
            return Collections.EMPTY_SET;
        } else {
            return partitions;
        }
    }

    public void setPartitions(Set<Short> partitions) {
        this.partitions = partitions;
    }

    public Set<Integer> getReplicas() {
        if (null == replicas) {
            return Collections.EMPTY_SET;
        } else {
            return replicas;
        }
    }

    public void setReplicas(Set<Integer> replicas) {
        this.replicas = replicas;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public Map<Integer, Broker> getBrokers() {
        return brokers;
    }

    public PartitionGroup.ElectType getElectType() {
        return electType;
    }

    public void setElectType(PartitionGroup.ElectType electType) {
        this.electType = electType;
    }

    public void setBrokers(Map<Integer, Broker> brokers) {
        this.brokers = brokers;
    }

    public Set<Integer> getLearners() {
        if (null == learners) {
            return Collections.EMPTY_SET;
        } else {
            return learners;
        }
    }

    public void setLearners(Set<Integer> learners) {
        this.learners = learners;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PartitionGroup)) return false;
        PartitionGroup that = (PartitionGroup) o;
        return group == that.group &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(partitions, that.partitions) &&
                Objects.equals(leader, that.leader) &&
                Objects.equals(recLeader, that.recLeader) &&
                Objects.equals(term, that.term) &&
                Objects.equals(replicas, that.replicas) &&
                Objects.equals(isrs, that.isrs) &&
                Objects.equals(learners, that.learners) &&
                electType == that.electType;
    }

    @Override
    public int hashCode() {

        return Objects.hash(topic, group, partitions, leader, recLeader, term, replicas, isrs, learners, electType);
    }

    public enum ElectType {
        //todo user value，not name
        fix(1),
        raft(0);
        private int type;

        ElectType(int type) {
            this.type = type;
        }

        public static ElectType valueOf(int type) {
            switch (type) {
                case 0:
                    return raft;
                case 1:
                    return fix;
                default:
                    return raft;
            }
        }
        public static ElectType value(String typeName) {
            if(fix.name().equals(typeName.toLowerCase())){
                return fix;
            }else{
                return  raft;
            }
        }
        public int type() {
            return type;
        }
    }

    @Override
    public PartitionGroup clone() {
        PartitionGroup partitionGroup = new PartitionGroup();
        partitionGroup.setTopic(topic);
        partitionGroup.setLeader(leader);
        partitionGroup.setRecLeader(recLeader);
        partitionGroup.setTerm(term);
        partitionGroup.setGroup(group);
        partitionGroup.setBrokers(null != brokers ? new HashMap<>(brokers) : new HashMap<>());
        partitionGroup.setElectType(getElectType());
        if (null != isrs) {
            partitionGroup.setIsrs(new TreeSet<>(isrs));
        }
        if (null != learners) {
            partitionGroup.setLearners(new TreeSet<>(learners));
        }

        if (null != partitions) {
            partitionGroup.setPartitions(new TreeSet<>(partitions));
        }
        if (null != replicas) {
            partitionGroup.setReplicas(new TreeSet<>(replicas));
        }
        return partitionGroup;
    }

    public List<Integer> getOutSyncReplicas() {
        return outSyncReplicas;
    }

    public void setOutSyncReplicas(List<Integer> outSyncReplicas) {
        this.outSyncReplicas = outSyncReplicas;
    }

    @Override
    public String toString() {
        return "PartitionGroup{" +
                "topic='" + topic + '\'' +
                ", leader=" + leader +
                ", isrs=" + isrs +
                ", learners=" + learners +
                ", term=" + term +
                ", group=" + group +
                ", partitions=" + partitions +
                ", replicas=" + replicas +
                ", brokers=" + brokers +
                ", electType=" + electType +
                '}';
    }

}
