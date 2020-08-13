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
package org.joyqueue.nsr.sql.domain;

import org.joyqueue.nsr.sql.helper.Column;

/**
 * PartitionGroupDTO
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class PartitionGroupDTO extends BaseDTO {

    private String id;
    private String topic;
    private String namespace;
    private Integer group;
    private String partitions;
    private Integer leader;
    @Column(alias = "rec_leader")
    private Integer recLeader;
    private Integer term;
    private String replicas;
    private String isrs;
    private String learners;
    @Column(alias = "out_sync_replicas")
    private String outSyncReplicas;
    @Column(alias = "elect_Type")
    private Byte electType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public String getPartitions() {
        return partitions;
    }

    public void setPartitions(String partitions) {
        this.partitions = partitions;
    }

    public Integer getLeader() {
        return leader;
    }

    public void setLeader(Integer leader) {
        this.leader = leader;
    }

    public Integer getRecLeader() {
        return recLeader;
    }

    public void setRecLeader(Integer recLeader) {
        this.recLeader = recLeader;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getReplicas() {
        return replicas;
    }

    public void setReplicas(String replicas) {
        this.replicas = replicas;
    }

    public String getIsrs() {
        return isrs;
    }

    public void setIsrs(String isrs) {
        this.isrs = isrs;
    }

    public String getLearners() {
        return learners;
    }

    public void setLearners(String learners) {
        this.learners = learners;
    }

    public String getOutSyncReplicas() {
        return outSyncReplicas;
    }

    public void setOutSyncReplicas(String outSyncReplicas) {
        this.outSyncReplicas = outSyncReplicas;
    }

    public void setElectType(Byte electType) {
        this.electType = electType;
    }

    public Byte getElectType() {
        return electType;
    }
}
