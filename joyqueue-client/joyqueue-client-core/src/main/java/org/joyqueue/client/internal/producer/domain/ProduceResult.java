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
package org.joyqueue.client.internal.producer.domain;

import java.io.Serializable;

/**
 * ProduceResult
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class ProduceResult implements Serializable {

    private String topic;
    private short partition;
    private long index;
    private long startTime;

    public ProduceResult(String topic, short partition, long index, long startTime) {
        this.topic = topic;
        this.partition = partition;
        this.index = index;
        this.startTime = startTime;
    }

    public String getTopic() {
        return topic;
    }

    public short getPartition() {
        return partition;
    }

    public long getIndex() {
        return index;
    }

    public long getStartTime() {
        return startTime;
    }
}