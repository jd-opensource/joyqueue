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
package org.joyqueue.nsr.network.command;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.network.transport.command.JoyQueuePayload;

public abstract class OperatePartitionGroup extends JoyQueuePayload {
    protected boolean rollback = false;
    protected PartitionGroup partitionGroup;

    public OperatePartitionGroup(PartitionGroup partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public OperatePartitionGroup(PartitionGroup partitionGroup, boolean rollback) {
        this.rollback = rollback;
        this.partitionGroup = partitionGroup;
    }

    public OperatePartitionGroup rollback(boolean rollback) {
        this.rollback = rollback;
        return this;
    }

    public boolean isRollback() {
        return rollback;
    }


    public PartitionGroup getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(PartitionGroup partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    @Override
    public String toString() {
        return "OperatePartitionGroup{" +
                "rollback=" + rollback +
                ", partitionGroup=" + partitionGroup +
                '}';
    }
}
