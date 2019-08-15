package io.chubao.joyqueue.broker.monitor.metrics;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;

/**
 * metrics
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class Metrics {

    private Meter meter;
    private Reservoir reservoir;
    private Histogram histogram;

    private long oneMinuteRate;

    public Metrics() {
        init();
    }

    public void slice() {
        oneMinuteRate = meter.getCount();
        reset();
    }

    public void reset() {
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

    public void mark(double time, long count) {
        this.meter.mark(count);
        this.histogram.update((long) time);
    }

    public void setCount(long count) {
        this.meter.mark(count);
    }

    public long getCount() {
        return this.meter.getCount();
    }

    public long getOneMinuteRate() {
        if (oneMinuteRate == 0) {
            return this.meter.getCount();
        } else {
            return oneMinuteRate;
        }
    }

    public long getMeanRate() {
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