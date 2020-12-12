package org.joyqueue.broker.archive;

import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.consumer.ConsumeConfig;
import org.joyqueue.broker.consumer.ConsumeConfigKey;
import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.broker.limit.support.AbstractSubscribeRateLimiterManager;
import org.joyqueue.broker.producer.ProduceConfig;
import org.joyqueue.broker.producer.ProducerConfigKey;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author majun8
 */
public class ArchiveRateLimiterManager extends AbstractSubscribeRateLimiterManager {
    protected static final Logger LOG = LoggerFactory.getLogger(ArchiveRateLimiterManager.class);

    private ProduceConfig produceConfig;
    private ConsumeConfig consumeConfig;

    public ArchiveRateLimiterManager(BrokerContext context) {
        super(context);
        this.produceConfig = new ProduceConfig(context != null ? context.getPropertySupplier() : null);;
        this.consumeConfig = new ConsumeConfig(context != null ? context.getPropertySupplier() : null);;
    }

    @Override
    public int producerLimitRate(String topic, String app) {
        int archiveRate = produceConfig.getArchiveRate(topic, app);
        if(archiveRate <= 0) {
            // get broker level retry rate
            archiveRate = produceConfig.getArchiveRate();
        }
        return archiveRate;
    }

    @Override
    public int consumerLimitRate(String topic, String app) {
        int archiveRate = consumeConfig.getArchiveRate(topic, app);
        if(archiveRate <= 0) {
            // get broker level retry rate
            archiveRate = consumeConfig.getArchiveRate();
        }
        return archiveRate;
    }

    @Override
    public void cleanRateLimiter(Config config) {
        String configKey = config.getKey();
        if (StringUtils.isBlank(configKey)) {
            return;
        }

        if (StringUtils.equals(configKey, ProducerConfigKey.PRODUCE_ARCHIVE_RATE.getName())) {
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
        } else if (StringUtils.startsWith(configKey, ProducerConfigKey.PRODUCE_ARCHIVE_RATE_PREFIX.getName())) {
            String[] keys = StringUtils.split(configKey, "\\.");
            if (keys != null && keys.length == 4) {
                String topic = keys[2];
                String app = keys[3];
                if (topic != null && app != null) {
                    cleanRateLimiter(topic, app, Subscription.Type.PRODUCTION);
                }
            }
        }

        if (StringUtils.equals(configKey, ConsumeConfigKey.CONSUME_ARCHIVE_RATE.getName())) {
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
        } else if (StringUtils.startsWith(configKey, ConsumeConfigKey.CONSUME_ARCHIVE_RATE_PREFIX.getName())) {
            String[] keys = StringUtils.split(configKey, "\\.");
            if (keys != null && keys.length == 4) {
                String topic = keys[2];
                String app = keys[3];
                if (topic != null && app != null) {
                    cleanRateLimiter(topic, app, Subscription.Type.CONSUMPTION);
                }
            }
        }
    }
}
