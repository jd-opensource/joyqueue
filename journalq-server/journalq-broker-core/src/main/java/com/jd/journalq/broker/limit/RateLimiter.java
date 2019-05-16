package com.jd.journalq.broker.limit;

/**
 * RateLimiter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class RateLimiter {

    private com.google.common.util.concurrent.RateLimiter tpsRateLimiter;
    private com.google.common.util.concurrent.RateLimiter trafficRateLimiter;

    public RateLimiter(int tps, int traffic) {
        this.tpsRateLimiter = com.google.common.util.concurrent.RateLimiter.create(tps);
        this.trafficRateLimiter = com.google.common.util.concurrent.RateLimiter.create(traffic);
    }

    public boolean tryAcquireTps() {
        return tryAcquireTps(1);
    }

    public boolean tryAcquireTps(int tps) {
        return tpsRateLimiter.tryAcquire(tps);
    }

    public boolean tryAcquireTraffic(int traffic) {
        return trafficRateLimiter.tryAcquire(traffic);
    }
}