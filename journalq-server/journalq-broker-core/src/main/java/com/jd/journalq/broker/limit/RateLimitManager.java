package com.jd.journalq.broker.limit;

/**
 * RateLimitManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/17
 */
public interface RateLimitManager {

    RateLimiter getRateLimiter(String topic, String app, String type);
}