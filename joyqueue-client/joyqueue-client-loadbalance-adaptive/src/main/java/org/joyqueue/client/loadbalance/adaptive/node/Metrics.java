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

    private Meter meter;
    private Reservoir reservoir;
    private Histogram histogram;

    public Metrics() {
        init();
    }

    public void slice() {
        init();
    }

    protected void init() {
        this.meter = new Meter();
        this.reservoir = new ExponentiallyDecayingReservoir();
        this.histogram = new Histogram(reservoir);
    }

    public void mark() {
        this.mark(1L);
    }

    public void mark(long count) {
        this.meter.mark(count);
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