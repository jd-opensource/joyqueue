package com.jd.journalq.broker.kafka.handler.ratelimit;

import com.jd.journalq.broker.kafka.handler.AbstractKafkaCommandHandler;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.util.RateLimiter;
import com.jd.journalq.toolkit.delay.DelayedOperationManager;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaRateLimitHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/12
 */
public class KafkaRateLimitHandlerFactory extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaRateLimitHandlerFactory.class);

    private KafkaConfig config;
    private DelayedOperationManager delayedOperationManager;
    private RateLimiter rateLimiter;

    public KafkaRateLimitHandlerFactory(KafkaConfig config, DelayedOperationManager delayedOperationManager, RateLimiter rateLimiter) {
        this.config = config;
        this.delayedOperationManager = delayedOperationManager;
        this.rateLimiter = rateLimiter;
    }

    public AbstractKafkaCommandHandler create(AbstractKafkaCommandHandler delegate) {
        return new KafkaRateLimitHandler(config, delegate, delayedOperationManager, rateLimiter);
    }

    @Override
    protected void doStart() throws Exception {
        delayedOperationManager.start();
    }

    @Override
    protected void doStop() {
        delayedOperationManager.shutdown();
    }
}