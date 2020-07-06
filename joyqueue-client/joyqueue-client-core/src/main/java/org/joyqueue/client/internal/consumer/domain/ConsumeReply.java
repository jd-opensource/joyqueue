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
package org.joyqueue.client.internal.consumer.domain;

import org.joyqueue.network.command.RetryType;

/**
 * ConsumeReply
 *
 * author: gaohaoxiang
 * date: 2018/12/12
 */
public class ConsumeReply {

    private short partition;
    private long index;
    private RetryType retryType = RetryType.NONE;

    public ConsumeReply() {

    }

    public ConsumeReply(short partition, long index) {
        this.partition = partition;
        this.index = index;
    }

    public ConsumeReply(short partition, long index, RetryType retryType) {
        this.partition = partition;
        this.index = index;
        this.retryType = retryType;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public short getPartition() {
        return partition;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return index;
    }

    public void setRetryType(RetryType retryType) {
        this.retryType = retryType;
    }

    public RetryType getRetryType() {
        return retryType;
    }

    @Override
    public String toString() {
        return "ConsumeReply{" +
                "partition=" + partition +
                ", index=" + index +
                ", retryType=" + retryType +
                '}';
    }
}