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
package org.joyqueue.broker.protocol.coordinator.assignment.domain;

/**
 * PartitionGroupAssignmentMetadata
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class PartitionGroupAssignmentMetadata {

    private int partitionGroupId;
    private int assigned;

    public int incrAssigned() {
        return assigned++;
    }

    public int decrAssigned() {
        return assigned--;
    }

    public int getPartitionGroupId() {
        return partitionGroupId;
    }

    public void setPartitionGroupId(int partitionGroupId) {
        this.partitionGroupId = partitionGroupId;
    }

    public void setAssigned(int assigned) {
        this.assigned = assigned;
    }

    public int getAssigned() {
        return assigned;
    }
}