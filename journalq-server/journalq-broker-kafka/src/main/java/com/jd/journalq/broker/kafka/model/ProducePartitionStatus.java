/**
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
package com.jd.journalq.broker.kafka.model;

/**
 * Created by zhangkepeng on 16-8-18.
 */
public class ProducePartitionStatus {

    public static final short NONE_OFFSET = 0;

    private int partition;
    private short errorCode;
    private long offset;

    public ProducePartitionStatus(short errorCode) {
        this.errorCode = errorCode;
    }

    public ProducePartitionStatus(short errorCode, long offset) {
        this.errorCode = errorCode;
        this.offset = offset;
    }

    public ProducePartitionStatus(int partition, long offset, short errorCode) {
        this.partition = partition;
        this.offset = offset;
        this.errorCode = errorCode;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

}
