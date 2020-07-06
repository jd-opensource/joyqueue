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
 * 入队信息
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class EnQueueMonitorInfo extends BaseMonitorInfo {

    private long count;
    private double tp99;
    private double tp90;
    private double max;
    private double min;
    private double avg;
    @Deprecated
    private long totalSize;

    // 分钟级
    @Deprecated
    private long oneMinuteRate;
    @Deprecated
    private long size;

    // 秒级
    private long tps;
    private long traffic;

    // 总体
    private long totalTraffic;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getOneMinuteRate() {
        return oneMinuteRate;
    }

    public void setOneMinuteRate(long oneMinuteRate) {
        this.oneMinuteRate = oneMinuteRate;
    }

    public double getTp99() {
        return tp99;
    }

    public void setTp99(double tp99) {
        this.tp99 = tp99;
    }

    public double getTp90() {
        return tp90;
    }

    public void setTp90(double tp90) {
        this.tp90 = tp90;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMin() {
        return min;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public double getAvg() {
        return avg;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public void setTps(long tps) {
        this.tps = tps;
    }

    public long getTps() {
        return tps;
    }

    public void setTraffic(long traffic) {
        this.traffic = traffic;
    }

    public long getTraffic() {
        return traffic;
    }

    public void setTotalTraffic(long totalTraffic) {
        this.totalTraffic = totalTraffic;
    }

    public long getTotalTraffic() {
        return totalTraffic;
    }
}