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
package org.joyqueue.broker.monitor.stat;

import org.joyqueue.toolkit.time.SystemClock;

import java.io.Serializable;

/**
 * PartitionStat
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class PartitionStat implements Serializable {

    private String topic;
    private String app;
    private short partition;

    private EnQueueStat enQueueStat = new EnQueueStat();
    private DeQueueStat deQueueStat = new DeQueueStat();

    //最后拉取时间
    private long lastPullTime = SystemClock.now();
    //最后应答时间
    private long lastAckTime = lastPullTime;

    private long ackIndex;
    private long right;

    public PartitionStat(String topic, String app, short partition) {
        this.topic = topic;
        this.app = app;
        this.partition = partition;
    }

    public long getLastPullTime() {
        return lastPullTime;
    }

    public long getLastAckTime() {
        return lastAckTime;
    }

    public void lastPullTime(long lastPullTime) {
        this.lastPullTime = lastPullTime;
    }

    public void lastAckTime(long lastAckTime){
        this.lastAckTime = lastAckTime;
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public short getPartition() {
        return partition;
    }

    public EnQueueStat getEnQueueStat() {
        return enQueueStat;
    }

    public DeQueueStat getDeQueueStat() {
        return deQueueStat;
    }

    public long getAckIndex() {
        return ackIndex;
    }

    public void setAckIndex(long ackIndex) {
        this.ackIndex = ackIndex;
    }

    public long getRight() {
        return right;
    }

    public void setRight(long right) {
        this.right = right;
    }
}
