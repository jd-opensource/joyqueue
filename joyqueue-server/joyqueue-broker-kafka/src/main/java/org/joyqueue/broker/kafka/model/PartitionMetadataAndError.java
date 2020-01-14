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
package org.joyqueue.broker.kafka.model;


/**
 * Created by zhangkepeng on 16-8-4.
 */
public class PartitionMetadataAndError {

    private int partition;
    private short error;

    public PartitionMetadataAndError() {

    }

    public PartitionMetadataAndError(int partition, short error) {
        this.partition = partition;
        this.error = error;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public short getError() {
        return error;
    }

    public void setError(short error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "PartitionMetadataAndError{" +
                "partition=" + partition +
                ", error=" + error +
                '}';
    }
}