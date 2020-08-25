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
 * TopicDTO
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class TopicDTO extends BaseDTO {

    private String id;
    private String code;
    private String namespace;
    private Short partitions;
    @Column(alias = "priority_partitions")
    private String priorityPartitions;
    private Byte type;
    private String policy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Short getPartitions() {
        return partitions;
    }

    public void setPartitions(Short partitions) {
        this.partitions = partitions;
    }

    public String getPriorityPartitions() {
        return priorityPartitions;
    }

    public void setPriorityPartitions(String priorityPartitions) {
        this.priorityPartitions = priorityPartitions;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getPolicy() {
        return policy;
    }
}