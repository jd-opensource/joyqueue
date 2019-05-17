package com.jd.journalq.broker.limit.support;

import com.jd.journalq.broker.limit.RateLimiter;

/**
 * NoneRateLimiter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/17
 */
public class NoneRateLimiter implements RateLimiter {

    @Override
    public boolean tryAcquireTps() {
        return true;
    }

    @Override
    public boolean tryAcquireTps(int tps) {
        return true;
    }

    @Override
    public boolean tryAcquireTraffic(int traffic) {
        return true;
    }
}