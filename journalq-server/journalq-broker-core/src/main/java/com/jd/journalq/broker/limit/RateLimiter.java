package com.jd.journalq.broker.limit;

/**
 * RateLimiter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public interface RateLimiter {

    boolean tryAcquireTps();

    boolean tryAcquireTps(int tps);

    boolean tryAcquireTraffic(int traffic);
}