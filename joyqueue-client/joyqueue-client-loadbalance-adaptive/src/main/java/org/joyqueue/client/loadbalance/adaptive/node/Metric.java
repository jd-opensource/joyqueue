package org.joyqueue.client.loadbalance.adaptive.node;

/**
 * Metric
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class Metric {

    private Metrics all = new Metrics();
    private Metrics error = new Metrics();

    public void refresh() {
        all.refresh();
        error.reinit();
    }

    public Tracer begin() {
        return new Tracer(this);
    }

    public void mark(int count, int time, boolean success) {
        this.all.mark(count, time);
        if (!success) {
            this.error.mark(count, time);
        }
    }

    public long getTps() {
        return all.getTps();
    }

    public long getErrorTps() {
        return error.getTps();
    }

    public long getCount() {
        return all.getCount();
    }

    public long getErrorCount() {
        return error.getCount();
    }

    public double getTp999() {
        return all.getTp999();
    }

    public double getTp99() {
        return all.getTp99();
    }

    public double getTp95() {
        return all.getTp95();
    }

    public double getTp75() {
        return all.getTp75();
    }

    public double getMax() {
        return all.getMax();
    }

    public double getMin() {
        return all.getMin();
    }

    public double getAvg() {
        return all.getAvg();
    }

    public static class Tracer {
        private Metric metric;
        private long startTime;

        public Tracer(Metric metric) {
            this.metric = metric;
            this.startTime = System.currentTimeMillis();
        }

        public void end(int count) {
            end(count, true);
        }

        public void error(int count) {
            end(count, false);
        }

        public void end() {
            end(1, true);
        }

        public void error() {
            end(1, false);
        }

        public void end(int count, boolean success) {
            metric.mark(count, (int) (System.currentTimeMillis() - startTime), success);
        }
    }
}