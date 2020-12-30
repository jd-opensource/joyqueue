package org.joyqueue.broker.archive;

import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.broker.limit.config.LimiterConfig;
import org.joyqueue.broker.limit.support.AbstractSubscribeRateLimiterManager;
import org.joyqueue.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author majun8
 */
public class ArchiveRateLimiterManager extends AbstractSubscribeRateLimiterManager {
    protected static final Logger logger = LoggerFactory.getLogger(ArchiveRateLimiterManager.class);

    private static final int DEFAULT_LIMIT_RATE = -1;

    private ClusterManager clusterManager;
    private ArchiveConfig archiveConfig;

    public ArchiveRateLimiterManager(BrokerContext context) {
        super(context);
        this.clusterManager = context.getClusterManager();
        this.archiveConfig = context.getArchiveManager().getArchiveConfig();
    }

    public int defaultProducerLimitRate(String topic, String app) {
        int archiveRate = archiveConfig.getProduceArchiveRate(topic, app);
        if(archiveRate <= 0) {
            // get broker level retry rate
            archiveRate = archiveConfig.getProduceArchiveRate();
        }
        return archiveRate;
    }

    public int defaultConsumerLimitRate(String topic, String app) {
        int archiveRate = archiveConfig.getConsumeArchiveRate(topic, app);
        if(archiveRate <= 0) {
            // get broker level retry rate
            archiveRate = archiveConfig.getConsumeArchiveRate();
        }
        return archiveRate;
    }

    @Override
    public LimiterConfig getLimiterConfig(String topic, String app, Subscription.Type subscribe) {
        TopicConfig topicConfig = clusterManager.getTopicConfig(TopicName.parse(topic));
        switch (subscribe) {
            case PRODUCTION:
                if (topicConfig != null) {
                    Topic.TopicPolicy policy = topicConfig.getPolicy();
                    Integer tps = DEFAULT_LIMIT_RATE;
                    if (policy != null && policy.getProduceArchiveTps() != null) {
                        tps = policy.getProduceArchiveTps();
                    } else {
                        tps = defaultProducerLimitRate(topic, app);
                    }
                    if (tps <= 0) {
                        tps = Integer.MAX_VALUE;
                    }
                    return new LimiterConfig(tps, -1);
                }
                break;
            case CONSUMPTION:
                if (topicConfig != null) {
                    Topic.TopicPolicy policy = topicConfig.getPolicy();
                    Integer tps = DEFAULT_LIMIT_RATE;
                    if (policy != null && policy.getConsumeArchiveTps() != null) {
                        tps = policy.getConsumeArchiveTps();
                    } else {
                        tps = defaultConsumerLimitRate(topic, app);
                    }
                    if (tps <= 0) {
                        tps = Integer.MAX_VALUE;
                    }
                    return new LimiterConfig(tps, -1);
                }
                break;
        }
        logger.error("unsupported limit type, topic: {}, app: {}, type: {}", topic, app, subscribe.name());
        return null;
    }

    @Override
    public void cleanRateLimiter(Config config) {
        String configKey = config.getKey();
        if (StringUtils.isBlank(configKey)) {
            return;
        }

        if (StringUtils.equals(configKey, ArchiveConfigKey.ARCHIVE_PRODUCE_RATE.getName())) {
            for (Map.Entry<String, ConcurrentMap<String, RateLimiter>> topic : subscribeRateLimiters.entrySet()) {
                Iterator<Map.Entry<String, RateLimiter>> subLimiters = topic.getValue().entrySet().iterator();
                while (subLimiters.hasNext()) {
                    Map.Entry<String, RateLimiter> subLimiter = subLimiters.next();
                    String subscribe = subLimiter.getKey();
                    if (StringUtils.contains(subscribe, Subscription.Type.PRODUCTION.name() + SPLIT)) {
                        subLimiters.remove();
                    }
                }
            }
        } else if (StringUtils.startsWith(configKey, ArchiveConfigKey.ARCHIVE_PRODUCE_RATE_PREFIX.getName())) {
            String[] keys = StringUtils.split(configKey, "\\.");
            if (keys != null) {
                if (keys.length == 4) {
                    // topic prefix
                    String topic = keys[3];
                    if (topic != null) {
                        cleanRateLimiter(topic, null, Subscription.Type.PRODUCTION);
                    }
                } else if (keys.length == 5) {
                    // topic & app prefix
                    String topic = keys[3];
                    String app = keys[4];
                    if (topic != null && app != null) {
                        cleanRateLimiter(topic, app, Subscription.Type.PRODUCTION);
                    }
                }
            }
        }

        if (StringUtils.equals(configKey, ArchiveConfigKey.ARCHIVE_CONSUME_RATE.getName())) {
            for (Map.Entry<String, ConcurrentMap<String, RateLimiter>> topic : subscribeRateLimiters.entrySet()) {
                Iterator<Map.Entry<String, RateLimiter>> subLimiters = topic.getValue().entrySet().iterator();
                while (subLimiters.hasNext()) {
                    Map.Entry<String, RateLimiter> subLimiter = subLimiters.next();
                    String subscribe = subLimiter.getKey();
                    if (StringUtils.contains(subscribe, Subscription.Type.CONSUMPTION.name() + SPLIT)) {
                        subLimiters.remove();
                    }
                }
            }
        } else if (StringUtils.startsWith(configKey, ArchiveConfigKey.ARCHIVE_CONSUME_RATE_PREFIX.getName())) {
            String[] keys = StringUtils.split(configKey, "\\.");
            if (keys != null && keys.length == 5) {
                String topic = keys[3];
                String app = keys[4];
                if (topic != null && app != null) {
                    cleanRateLimiter(topic, app, Subscription.Type.CONSUMPTION);
                }
            }
        }
    }
}
