package org.joyqueue.broker.limit.support;

import com.google.common.collect.Maps;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.broker.limit.SubscribeRateLimiter;
import org.joyqueue.broker.limit.config.LimiterConfig;
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
    protected static final Logger logger = LoggerFactory.getLogger(AbstractSubscribeRateLimiterManager.class);

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
        String subscribeName = app == null ? subscribe.name() + SPLIT : subscribe.name() + SPLIT + app;
        RateLimiter subscribeRateLimiter = topicRateLimiters.get(subscribeName);
        if (subscribeRateLimiter == null) {
            LimiterConfig config = getLimiterConfig(topic, app, subscribe);
            subscribeRateLimiter = new DefaultRateLimiter(config.getTps());
            RateLimiter oldRateLimiter = topicRateLimiters.putIfAbsent(subscribeName, subscribeRateLimiter);
            if (oldRateLimiter != null) {
                subscribeRateLimiter = oldRateLimiter;
            } else {
                logger.info("Archive rate limiter for {},{},{},{}", topic, app, subscribe.name(), config);
            }
        }
        return subscribeRateLimiter;
    }

    public RateLimiter getOrCreate(String topic, Subscription.Type subscribe) {
        return getOrCreate(topic, null, subscribe);
    }

    public abstract LimiterConfig getLimiterConfig(String topic, String app, Subscription.Type subscribe);

    @Override
    public void onEvent(MetaEvent event) {
        switch (event.getEventType()) {
            case ADD_CONFIG: {
                AddConfigEvent addConfigEvent = (AddConfigEvent) event;
                Config config = addConfigEvent.getConfig();
                cleanRateLimiter(config);
                break;
            }
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
            case ADD_TOPIC:
                AddTopicEvent addTopicEvent = (AddTopicEvent) event;
                cleanRateLimiter(addTopicEvent.getTopic().getName().getFullName(), null, null);
                break;
            case UPDATE_TOPIC:
                UpdateTopicEvent updateTopicEvent = (UpdateTopicEvent) event;
                cleanRateLimiter(updateTopicEvent.getNewTopic().getName().getFullName(), null, null);
                break;
            case REMOVE_TOPIC:
                RemoveTopicEvent removeTopicEvent = (RemoveTopicEvent) event;
                cleanRateLimiter(removeTopicEvent.getTopic().getName().getFullName(), null, null);
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
                        Subscription.Type.PRODUCTION);
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
                        Subscription.Type.CONSUMPTION);
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
