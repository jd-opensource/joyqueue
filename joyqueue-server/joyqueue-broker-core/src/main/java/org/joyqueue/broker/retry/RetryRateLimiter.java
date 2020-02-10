package org.joyqueue.broker.retry;

import org.joyqueue.broker.limit.RateLimiter;

/**
 * Retry rate limiter interface
 *
 **/
public interface RetryRateLimiter {

    /**
     * Get or create a rate limiter
     **/
    RateLimiter getOrCreate(String topic, String app);
}
