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
package org.joyqueue.broker.consumer.position.model;

/**
 * 消费清单，描述消费者在分区口径的消费情况
 * <p>
 * Created by chengzhiliang on 2019/2/28.
 */
public class ConsumeBill {
    // 分区编号
    private int partitionGroup;
    // 分区编号
    private short partition;
    // 应答消息开始序号
    private long ackStartIndex;
    // 应答消息当前序号
    private long ackCurIndex;
    // 拉取消息序号
    private long pullStartIndex;
    // 拉取消息当前序号
    private long pullCurIndex;

    public ConsumeBill() {

    }

    public ConsumeBill(int partitionGroup, short partition, Position position) {
        this.partitionGroup = partitionGroup;
        this.partition = partition;
        this.ackStartIndex = position.getAckStartIndex();
        this.ackCurIndex = position.getAckCurIndex();
        this.pullStartIndex = position.getPullStartIndex();
        this.pullCurIndex = position.getPullCurIndex();
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getAckStartIndex() {
        return ackStartIndex;
    }

    public void setAckStartIndex(long ackStartIndex) {
        this.ackStartIndex = ackStartIndex;
    }

    public long getAckCurIndex() {
        return ackCurIndex;
    }

    public void setAckCurIndex(long ackCurIndex) {
        this.ackCurIndex = ackCurIndex;
    }

    public long getPullStartIndex() {
        return pullStartIndex;
    }

    public void setPullStartIndex(long pullStartIndex) {
        this.pullStartIndex = pullStartIndex;
    }

    public long getPullCurIndex() {
        return pullCurIndex;
    }

    public void setPullCurIndex(long pullCurIndex) {
        this.pullCurIndex = pullCurIndex;
    }
}
