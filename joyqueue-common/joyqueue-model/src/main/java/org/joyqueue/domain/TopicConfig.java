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

import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lixiaobin6
 */
public class TopicConfig extends Topic implements Serializable {

    /**
     * Topic所有partitionGroup信息
     * &lt; broker,id-PartitionGroup&gt;
     * -1表示还没有leader的broker的partitionGroup;
     */
    private Map<Short, PartitionGroup> partitionGroupMap = new HashMap<>();

    private Map<Integer,PartitionGroup> partitionGroups;

    /**
     * 主题配置转换
     *
     * @param topic 主题
     * @return 主题配置
     */
    public static TopicConfig toTopicConfig(Topic topic) {
        if (topic == null) {
            return null;
        }
        TopicConfig config = new TopicConfig();
        //设置主题
        config.setName(topic.getName());
        config.setPartitions(topic.getPartitions());
        config.setType(topic.getType());
        config.setPriorityPartitions(topic.getPriorityPartitions());
        config.setPolicy(topic.getPolicy());
        return config;
    }

    public static TopicConfig toTopicConfig(Topic topic, List<PartitionGroup> partitionGroups) {
        if (topic == null) {
            return null;
        }
        TopicConfig config = toTopicConfig(topic);
        Map<Integer, PartitionGroup> partitionGroupMap = Maps.newHashMap();
        for (PartitionGroup partitionGroup : partitionGroups) {
            partitionGroupMap.put(partitionGroup.getGroup(), partitionGroup);
        }
        config.setPartitionGroups(partitionGroupMap);
        return config;
    }

    public TopicConfig clone() {
        TopicConfig topicConfig = toTopicConfig(this);
        Map<Integer, PartitionGroup> partitionGroups = Maps.newHashMap();
        for (Map.Entry<Integer, PartitionGroup> entry : getPartitionGroups().entrySet()) {
            partitionGroups.put(entry.getKey(), entry.getValue().clone());
        }
        topicConfig.setPartitionGroups(partitionGroups);
        return topicConfig;
    }

    public Map<Integer,PartitionGroup> getPartitionGroups() {
        return partitionGroups;
    }

    public boolean isReplica(int brokerId) {
        for (Map.Entry<Integer, PartitionGroup> entry : partitionGroups.entrySet()) {
            if (entry.getValue().getReplicas().contains(brokerId)) {
                return true;
            }
        }
        return false;
    }

    public List<PartitionGroup> fetchTopicPartitionGroupsByBrokerId(int brokerId) {
        List<PartitionGroup> list = new ArrayList<>();
        for(PartitionGroup group : partitionGroups.values()) {
            if (group.getReplicas().contains(brokerId)){
                list.add(group);
            }
        }
        return list;
    }

    public void setPartitionGroups(Map<Integer,PartitionGroup> partitionGroups) {
        this.partitionGroups = partitionGroups;
        this.partitionGroupMap = buildPartitionGroupMap(partitionGroups);
    }

    private Map<Short, PartitionGroup> buildPartitionGroupMap(Map<Integer,PartitionGroup> partitionGroups) {
        Map<Short, PartitionGroup> result = Maps.newHashMap();
        if (MapUtils.isEmpty(partitionGroups)) {
            return result;
        }
        for (PartitionGroup partitionGroup : partitionGroups.values()) {
            for (Short partition : partitionGroup.getPartitions()) {
                result.put(partition, partitionGroup);
            }
        }
        return result;
    }

    public PartitionGroup fetchPartitionGroupByPartition(short partition) {
        return partitionGroupMap.get(partition);
    }

    public PartitionGroup fetchPartitionGroupByGroup(int group) {
        return partitionGroups.get(group);
    }

    public Broker fetchBrokerByPartition(short partition) {
        PartitionGroup group = fetchPartitionGroupByPartition(partition);
        if (null != group){
            return group.getBrokers().get(group.getLeader());
        }
        return null;
    }

    public List<Partition> fetchPartitionMetadata() {
        List<Partition> metadataList = new ArrayList<>();
            for (PartitionGroup group : partitionGroups.values()) {
                for (Short partition : group.getPartitions()) {
                    Set<Broker> isrs = new HashSet<>(null == group.getIsrs() ? 0 : group.getIsrs().size());
                    Set<Broker> replicas = new HashSet<>(null == group.getReplicas() ? 0 : group.getReplicas().size());
                    if (null != group.getIsrs()){
                        for (Integer brokerId : group.getIsrs()) {
                            if (group.getBrokers().get(brokerId) != null) {
                                isrs.add(group.getBrokers().get(brokerId));
                            }
                        }
                    }
                    if (null != group.getReplicas()){
                        for (Integer brokerId : group.getReplicas()) {
                            if (group.getBrokers().get(brokerId) != null) {
                                replicas.add(group.getBrokers().get(brokerId));
                            }
                        }
                    }
                    metadataList.add(new Partition(partition, group.getBrokers().get(group.getLeader()), replicas, isrs));
                }
            }
        return metadataList;
    }

    public Map<Integer, Broker> fetchAllBroker() {
        Map<Integer, Broker> brokers = new HashMap<>();
            for (PartitionGroup group : partitionGroups.values()) {
                brokers.putAll(group.getBrokers());
            }
        return brokers;
    }

    /**
     * 返回相关的broker(获取topic中所有partition的replicas)
     * @return all brokers
     */
    public Set<Integer> fetchAllBrokerIds() {
        Set<Integer> brokers = new HashSet<>();
        for (PartitionGroup group : partitionGroups.values()) {
            brokers.addAll(group.getReplicas());
        }
        return brokers;
    }
    public Set<Short> fetchAllPartitions() {
        Set<Short> partitions = new HashSet<>();
        for (PartitionGroup group : partitionGroups.values()) {
            partitions.addAll(group.getPartitions());
        }
        return partitions;
    }

    public boolean checkSequential() {
        //TODO 默认都设置为非顺序消息
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TopicConfig)) {
            return false;
        }
        if (o == this) {
            return true;
        }

        return super.equals(o);
    }

    @Override
    public String toString() {
        return "TopicConfig{" +
                "topic='" + name.getFullName() + '\'' +
                ", partitions=" + partitions +
                ", type=" + type +
                ", priorityPartitions=" + (null == priorityPartitions ? "[]" : Arrays.toString(priorityPartitions.toArray())) +
                ", partitionGroups=" + partitionGroups +
                ", partitionGroupMap=" + partitionGroupMap +
                '}';
    }
}
