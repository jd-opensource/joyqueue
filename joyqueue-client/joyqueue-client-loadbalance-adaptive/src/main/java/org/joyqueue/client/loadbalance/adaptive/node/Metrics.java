package org.joyqueue.client.loadbalance.adaptive.node;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;

/**
 * Metrics
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class Metrics {

    public static int cacheInterval = 1000 * 1;
    public static int sliceInterval = 1000 * 60;

    private volatile Meter meter;
    private volatile Reservoir reservoir;
    private volatile Histogram histogram;

    private volatile long lastSlice;

    private volatile double lastAvg;
    private volatile long lastAvgTime;

    public Metrics() {
        init();
    }

    public void init() {
        this.meter = new Meter();
        this.reservoir = new ExponentiallyDecayingReservoir();
        this.histogram = new Histogram(reservoir);
    }

    public void slice() {
        this.reservoir = new ExponentiallyDecayingReservoir();
        this.histogram = new Histogram(reservoir);
    }

    public void refresh() {
        if (System.currentTimeMillis() - lastSlice > sliceInterval) {
            lastSlice = System.currentTimeMillis();
            slice();
        }
    }

    public void reinit() {
        if (System.currentTimeMillis() - lastSlice > sliceInterval) {
            lastSlice = System.currentTimeMillis();
            init();
        }
    }

    public void mark(long count, double time) {
        this.meter.mark(count);
        this.histogram.update((long) time);
    }

    public void setCount(long count) {
        this.meter.mark(count);
    }

    public long getCount() {
        return this.meter.getCount();
    }

    public long getTps() {
        return (long) this.meter.getMeanRate();
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

    public double getMax() {
        return this.getSnapshot().getMax();
    }

    public double getMin() {
        return this.getSnapshot().getMin();
    }

    public double getAvg() {
        if (System.currentTimeMillis() - lastAvgTime > cacheInterval) {
            lastAvg = this.getSnapshot().getMean();
            lastAvgTime = System.currentTimeMillis();
        }
        return lastAvg;
    }

    protected Snapshot getSnapshot() {
        return this.histogram.getSnapshot();
    }
}