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
package org.joyqueue.nsr.ignite.model;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author wylixiaobin
 * Date: 2018/8/17
 */
public class IgnitePartitionGroup extends PartitionGroup implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMESPACE = "namespace";
    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_GROUP = "group";
    public static final String COLUMN_LEADER = "leader";
    public static final String COLUMN_ISR = "isr";
    public static final String COLUMN_TERM = "term";
    public static final String COLUMN_PARTITIONS = "partitions";
    public static final String COLUMN_LEARNERS = "learners";
    public static final String COLUMN_REPLICAS = "replicas";
    public static final String COLUMN_OUT_SYNC_REPLICAS = "out_sync_replicas";
    public static final String COLUMN_ELECT_TYPE = "elect_Type";
    public static final String COLUMN_REC_LEADER = "rec_leader";

    public IgnitePartitionGroup(PartitionGroup partitionGroup) {
        this.partitions = partitionGroup.getPartitions();
        this.topic = partitionGroup.getTopic();
        this.term = partitionGroup.getTerm();
        this.isrs = partitionGroup.getIsrs();
        this.replicas = partitionGroup.getReplicas();
        this.leader = partitionGroup.getLeader();
        this.electType = partitionGroup.getElectType();
        this.group = partitionGroup.getGroup();
        this.recLeader = partitionGroup.getRecLeader();
    }

    /**
     * id:namespace+topic+group
     */
    @Override
    public String getId() {
        return new StringBuilder(30).append(topic.getFullName()).append(SPLICE).append(group).toString();
    }


    public static String getId(TopicName topic, int group) {
        return new StringBuilder(30).append(topic.getFullName()).append(SPLICE).append(group).toString();
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_ID, this.getId());
        writer.writeString(COLUMN_NAMESPACE, topic.getNamespace());
        writer.writeString(COLUMN_TOPIC, topic.getCode());
        writer.writeInt(COLUMN_GROUP, this.getGroup());
        if (null != leader) writer.writeInt(COLUMN_LEADER, leader);
        if (null != isrs) writer.writeString(COLUMN_ISR, Arrays.toString(isrs.toArray()));
        if (null != term) writer.writeInt(COLUMN_TERM, term);
        writer.writeString(COLUMN_PARTITIONS, Arrays.toString(partitions.toArray()));
        writer.writeString(COLUMN_REPLICAS, Arrays.toString(replicas.toArray()));
        writer.writeString(COLUMN_ELECT_TYPE, electType.name());
        writer.writeInt(COLUMN_REC_LEADER, recLeader);
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        //this.id = reader.readString(COLUMN_ID);
        this.topic = new TopicName(reader.readString(COLUMN_TOPIC), reader.readString(COLUMN_NAMESPACE));
        this.group = reader.readInt(COLUMN_GROUP);
        this.leader = reader.readInt(COLUMN_LEADER);
        this.term = reader.readInt(COLUMN_TERM);
        String isrStr = reader.readString(COLUMN_ISR);
        String partitionsStr = reader.readString(COLUMN_PARTITIONS);
        String replicasStr = reader.readString(COLUMN_REPLICAS);
        String column_learnersStr = reader.readString(COLUMN_LEARNERS);
        this.isrs = new TreeSet<>();
        this.learners = new TreeSet<>();
        this.partitions = new TreeSet<>();
        this.replicas = new TreeSet<>();
        if (StringUtils.isNoneEmpty(isrStr) && isrStr.length() > 2) {
            isrs.addAll(Arrays.stream(isrStr.substring(1, isrStr.length() - 1).split(",")).map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList()));
        }
        if (StringUtils.isNoneEmpty(column_learnersStr) && column_learnersStr.length() > 2) {
            this.learners.addAll(Arrays.stream(isrStr.substring(1, column_learnersStr.length() - 1).split(",")).map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList()));
        }
        if (StringUtils.isNoneEmpty(partitionsStr) && partitionsStr.length() > 2) {
            this.partitions.addAll(Arrays.stream(partitionsStr.substring(1, partitionsStr.length() - 1).split(",")).map(s -> Short.parseShort(s.trim())).collect(Collectors.toList()));
        }
        if (StringUtils.isNoneEmpty(replicasStr) && replicasStr.length() > 2) {
            this.replicas.addAll(Arrays.stream(replicasStr.substring(1, replicasStr.length() - 1).split(",")).map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList()));
        }
        this.setElectType(ElectType.value(reader.readString(COLUMN_ELECT_TYPE)));
        this.recLeader = reader.readInt(COLUMN_REC_LEADER);
    }
}
