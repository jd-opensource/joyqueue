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
package org.joyqueue.monitor;

/**
 * PartitionAckMonitorInfo
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class PartitionAckMonitorInfo extends BaseMonitorInfo {

    private short partition;
    private long index;
    private long lastPullTime;
    private long lastAckTime;
    private long leftIndex;
    private long rightIndex;
    private long tps;
    private long traffic;

    public PartitionAckMonitorInfo() {

    }

    public PartitionAckMonitorInfo(short partition, long index, long lastPullTime, long lastAckTime, long leftIndex, long rightIndex) {
        this.partition = partition;
        this.index = index;
        this.lastPullTime = lastPullTime;
        this.lastAckTime = lastAckTime;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getLastPullTime() {
        return lastPullTime;
    }

    public void setLastPullTime(long lastPullTime) {
        this.lastPullTime = lastPullTime;
    }

    public long getLastAckTime() {
        return lastAckTime;
    }

    public void setLastAckTime(long lastAckTime) {
        this.lastAckTime = lastAckTime;
    }

    public void setLeftIndex(long leftIndex) {
        this.leftIndex = leftIndex;
    }

    public long getLeftIndex() {
        return leftIndex;
    }

    public void setRightIndex(long rightIndex) {
        this.rightIndex = rightIndex;
    }

    public long getRightIndex() {
        return rightIndex;
    }

    public long getTps() {
        return tps;
    }

    public void setTps(long tps) {
        this.tps = tps;
    }

    public long getTraffic() {
        return traffic;
    }

    public void setTraffic(long traffic) {
        this.traffic = traffic;
    }
}