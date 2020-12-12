package org.joyqueue.broker.limit.support;

import com.google.common.collect.Maps;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.broker.limit.SubscribeRateLimiter;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Subscription;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.nsr.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author majun8
 */
public abstract class AbstractSubscribeRateLimiterManager implements SubscribeRateLimiter {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractSubscribeRateLimiterManager.class);

    protected static final String SPLIT = ".";

    protected ClusterManager clusterManager;
    protected ConcurrentMap<String /** topic **/, ConcurrentMap<String /** app **/, RateLimiter>> subscribeRateLimiters = Maps.newConcurrentMap();

    public AbstractSubscribeRateLimiterManager(BrokerContext context) {
        this.clusterManager = context.getClusterManager();
        this.clusterManager.addListener(this);
    }

    public RateLimiter getOrCreate(String topic, String app, Subscription.Type subscribe) {
        ConcurrentMap<String, RateLimiter> topicRateLimiters = subscribeRateLimiters.get(topic);
        if (topicRateLimiters == null) {
            topicRateLimiters = new ConcurrentHashMap<>();
            ConcurrentMap<String, RateLimiter> old = subscribeRateLimiters.putIfAbsent(topic, topicRateLimiters);
            if (old != null) {
                topicRateLimiters = old;
            }
        }
        RateLimiter subscribeRateLimiter = topicRateLimiters.get(subscribe.name() + SPLIT + app);
        if (subscribeRateLimiter == null) {
            switch (subscribe) {
                case PRODUCTION:
                    int pTps = producerLimitRate(topic, app);
                    if (pTps > 0) {
                        subscribeRateLimiter = new DefaultRateLimiter(pTps);
                        RateLimiter oldRateLimiter = topicRateLimiters.putIfAbsent(subscribe.name() + SPLIT + app, subscribeRateLimiter);
                        if (oldRateLimiter != null) {
                            subscribeRateLimiter = oldRateLimiter;
                        } else {
                            LOG.info("New produce archive rate limiter for {},{},{},{}", topic, app, subscribe.name(), pTps);
                        }
                    }
                    break;
                case CONSUMPTION:
                    int cTps = consumerLimitRate(topic, app);
                    if (cTps > 0) {
                        subscribeRateLimiter = new DefaultRateLimiter(cTps);
                        RateLimiter oldRateLimiter = topicRateLimiters.putIfAbsent(subscribe.name() + SPLIT + app, subscribeRateLimiter);
                        if (oldRateLimiter != null) {
                            subscribeRateLimiter = oldRateLimiter;
                        } else {
                            LOG.info("New consume archive rate limiter for {},{},{},{}", topic, app, subscribe.name(), cTps);
                        }
                    }
                    break;
            }

        }
        return subscribeRateLimiter;
    }

    public abstract int producerLimitRate(String topic, String app);

    public abstract int consumerLimitRate(String topic, String app);

    @Override
    public void onEvent(MetaEvent event) {
        switch (event.getEventType()) {
            case UPDATE_CONFIG: {
                UpdateConfigEvent updateConfigEvent = (UpdateConfigEvent) event;
                Config config = updateConfigEvent.getNewConfig();
                cleanRateLimiter(config);
                break;
            }
            case REMOVE_CONFIG: {
                RemoveConfigEvent removeConfigEvent = (RemoveConfigEvent) event;
                Config config = removeConfigEvent.getConfig();
                cleanRateLimiter(config);
                break;
            }
            case REMOVE_TOPIC:
                RemoveTopicEvent topicEvent = (RemoveTopicEvent) event;
                cleanRateLimiter(topicEvent.getTopic().getName().getFullName(), null, null);
                break;
            case UPDATE_PRODUCER:
                UpdateProducerEvent updateProducerEvent = (UpdateProducerEvent) event;
                cleanRateLimiter(updateProducerEvent.getTopic().getFullName(),
                        updateProducerEvent.getNewProducer().getApp(),
                        Subscription.Type.PRODUCTION);
                break;
            case REMOVE_PRODUCER:
                RemoveProducerEvent removeProducerEvent = (RemoveProducerEvent) event;
                cleanRateLimiter(removeProducerEvent.getTopic().getFullName(),
                        removeProducerEvent.getProducer().getApp(),
                        Subscription.Type.PRODUCTION
                );
                break;
            case UPDATE_CONSUMER:
                UpdateConsumerEvent updateConsumerEvent = (UpdateConsumerEvent) event;
                cleanRateLimiter(updateConsumerEvent.getTopic().getFullName(),
                        updateConsumerEvent.getNewConsumer().getApp(),
                        Subscription.Type.CONSUMPTION);
                break;
            case REMOVE_CONSUMER:
                RemoveConsumerEvent removeConsumerEvent = (RemoveConsumerEvent) event;
                cleanRateLimiter(removeConsumerEvent.getTopic().getFullName(),
                        removeConsumerEvent.getConsumer().getApp(),
                        Subscription.Type.CONSUMPTION
                );
                break;
        }
    }

    public abstract void cleanRateLimiter(Config config);

    public void cleanRateLimiter(String topic, String app, Subscription.Type subscribe) {
        if (app == null) {
            subscribeRateLimiters.remove(topic);
        } else {
            Map<String, RateLimiter> rateLimiters = subscribeRateLimiters.get(topic);
            if (rateLimiters != null) {
                rateLimiters.remove(subscribe.name() + SPLIT + app);
            }
        }
    }
}
