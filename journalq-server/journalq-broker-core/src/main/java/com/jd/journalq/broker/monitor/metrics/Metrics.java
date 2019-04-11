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
package com.jd.journalq.broker.monitor.metrics;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;

import java.util.concurrent.atomic.LongAdder;

/**
 * metrics
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/11
 */
public class Metrics {

    private Meter meter;
    private LongAdder counter;
    private Reservoir reservoir;
    private Histogram histogram;

    public Metrics() {
        this.meter = new Meter();
        this.counter = new LongAdder();
        this.reservoir = new ExponentiallyDecayingReservoir();
        this.histogram = new Histogram(reservoir);
    }

    public void mark() {
        this.mark(1L);
    }

    public void mark(long count) {
        this.counter.add(count);
        this.meter.mark(count);
    }

    public void mark(long time, long count) {
        this.counter.add(count);
        this.meter.mark(count);
        this.histogram.update(time);
    }

    public void setCount(long count) {
        this.counter.reset();
        this.counter.add(count);
    }

    public long getCount() {
        return this.counter.longValue();
    }

    public long getOneMinuteRate() {
        return (long) this.meter.getOneMinuteRate();
    }

    public long getFiveMinuteRate() {
        return (long) this.meter.getFiveMinuteRate();
    }

    public long getFifteenMinuteRate() {
        return (long) this.meter.getFifteenMinuteRate();
    }

    public double getTp999() {
        return this.getSnapshot().get999thPercentile();
    }

    public double getTp99() {
        return this.getSnapshot().get99thPercentile();
    }

    public double getTp95() {
        return this.getSnapshot().get95thPercentile();
    }

    public double getTp75() {
        return this.getSnapshot().get75thPercentile();
    }

    public double getTp90() {
        return this.getSnapshot().getMean();
    }

    public double getMax() {
        return this.getSnapshot().getMax();
    }

    public double getMin() {
        return this.getSnapshot().getMin();
    }

    public double getAvg() {
        return this.getSnapshot().getMean();
    }

    protected Snapshot getSnapshot() {
        return this.histogram.getSnapshot();
    }
}