package org.joyqueue.broker.limit;

import org.joyqueue.domain.Subscription;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.toolkit.concurrent.EventListener;

/**
 * Subscribe limiter interface
 *
 **/
public interface SubscribeRateLimiter extends EventListener<MetaEvent> {

    /**
     * Get or create a rate limiter according to subscribe type
     * @return null indicate no limit
     **/
    RateLimiter getOrCreate(String topic, String app, Subscription.Type subscribe);

    RateLimiter getOrCreate(String topic, Subscription.Type subscribe);
}
