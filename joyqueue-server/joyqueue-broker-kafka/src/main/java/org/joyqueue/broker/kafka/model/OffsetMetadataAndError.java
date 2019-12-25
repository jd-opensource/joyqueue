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
public class OffsetMetadataAndError {

    private int partition;
    private long offset;
    private String metadata = OffsetAndMetadata.NO_METADATA;
    private short error;

    public OffsetMetadataAndError(short error) {
        this.error = error;
    }

    public OffsetMetadataAndError(int partition, short error) {
        this.partition = partition;
        this.error = error;
    }

    public OffsetMetadataAndError(long offset, String metadata, short error) {
        this.offset = offset;
        this.metadata = metadata;
        this.error = error;
    }

    public OffsetMetadataAndError(int partition, long offset, String metadata, short error) {
        this.partition = partition;
        this.offset = offset;
        this.metadata = metadata;
        this.error = error;
    }

    public OffsetMetadataAndError(int partition, long offset, short error) {
        this.partition = partition;
        this.offset = offset;
        this.error = error;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public String getMetadata() {
        return metadata;
    }

    public short getError() {
        return error;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setError(short error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return String.format("OffsetMetadataAndError[%d, %d,%s,%d]", partition, offset, metadata, error);
    }

}