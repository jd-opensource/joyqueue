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
package com.jd.journalq.broker.monitor.stat;

import com.jd.journalq.broker.monitor.metrics.Metrics;

/**
 * DeQueueStat
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/11
 */
public class DeQueueStat {

    private Metrics count;
    private Metrics size;

    public DeQueueStat() {
        this.count = new Metrics();
        this.size = new Metrics();
    }

    public void mark(long time, long size, long count) {
        this.count.mark(time, count);
        this.size.mark(size);
    }

    public void setTotal(long total) {
        this.count.setCount(total);
    }

    public void setTotalSize(long totalSize) {
        this.size.setCount(totalSize);
    }

    public long getTotal() {
        return this.count.getCount();
    }

    public long getTotalSize() {
        return this.size.getCount();
    }

    public long getOneMinuteRate() {
        return this.count.getOneMinuteRate();
    }

    public double getTp99() {
        return this.count.getTp99();
    }

    public double getTp90() {
        return this.count.getTp90();
    }

    public double getMax() {
        return this.count.getMax();
    }

    public double getMin() {
        return this.count.getMin();
    }

    public double getAvg() {
        return this.count.getAvg();
    }

    public long getSize() {
        return this.size.getOneMinuteRate();
    }
}