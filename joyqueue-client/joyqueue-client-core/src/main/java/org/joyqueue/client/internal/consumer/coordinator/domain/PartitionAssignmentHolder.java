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
package org.joyqueue.client.internal.consumer.coordinator.domain;

/**
 * PartitionAssignmentHolder
 *
 * author: gaohaoxiang
 * date: 2018/12/11
 */
public class PartitionAssignmentHolder {

    private PartitionAssignment partitionAssignment;
    private long createTime;

    public PartitionAssignmentHolder(PartitionAssignment partitionAssignment, long createTime) {
        this.partitionAssignment = partitionAssignment;
        this.createTime = createTime;
    }

    public PartitionAssignment getPartitionAssignment() {
        return partitionAssignment;
    }

    public void setPartitionAssignment(PartitionAssignment partitionAssignment) {
        this.partitionAssignment = partitionAssignment;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}