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
 * PartitionGroupReplicaDTO
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class PartitionGroupReplicaDTO extends BaseDTO {

    private String id;
    private String topic;
    private String namespace;
    @Column(alias = "broker_id")
    private Long brokerId;
    private Integer group;

    public PartitionGroupReplicaDTO() {

    }

    public PartitionGroupReplicaDTO(String id, String topic, String namespace, Long brokerId, Integer group) {
        this.id = id;
        this.topic = topic;
        this.namespace = namespace;
        this.brokerId = brokerId;
        this.group = group;
    }

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

    public Long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }
}