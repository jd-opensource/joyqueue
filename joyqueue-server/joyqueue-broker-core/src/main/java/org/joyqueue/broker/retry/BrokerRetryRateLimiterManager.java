package org.joyqueue.broker.retry;

import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.consumer.ConsumeConfig;
import org.joyqueue.broker.consumer.ConsumeConfigKey;
import org.joyqueue.broker.limit.config.LimiterConfig;
import org.joyqueue.broker.limit.support.AbstractSubscribeRateLimiterManager;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer retry rate limiter manager
 *
 **/
public class BrokerRetryRateLimiterManager extends AbstractSubscribeRateLimiterManager {
    protected static final Logger LOG = LoggerFactory.getLogger(BrokerRetryRateLimiterManager.class);

    private ConsumeConfig consumeConfig;

    public BrokerRetryRateLimiterManager(BrokerContext context){
        super(context);
        this.consumeConfig=new ConsumeConfig(context != null ? context.getPropertySupplier() : null);
    }

    @Override
    public LimiterConfig getLimiterConfig(String topic, String app, Subscription.Type subscribe) {
        if (subscribe == Subscription.Type.CONSUMPTION) {
            int tps = defaultConsumerLimitRate(topic, app);
            if (tps <= 0) {
                tps = Integer.MAX_VALUE;
            }
            return new LimiterConfig(tps, -1);
        }
        return null;
    }

    public int defaultProducerLimitRate(String topic, String app) {
        return 0;
    }

    public int defaultConsumerLimitRate(String topic, String app) {
        int retryRate = consumeConfig.getRetryRate(topic, app);
        if (retryRate <= 0) {
            // get broker level retry rate
            retryRate = consumeConfig.getRetryRate();
        }
        return retryRate;
    }

    /**
     * @param config  consumer config
     *
     **/
    public void cleanRateLimiter(Config config) {
        String configKey = config.getKey();
        if (StringUtils.isBlank(configKey)) {
            return;
        }

        if (StringUtils.equals(configKey, ConsumeConfigKey.RETRY_RATE.getName())) {
            subscribeRateLimiters.clear();
        } else if (StringUtils.startsWith(configKey, ConsumeConfigKey.RETRY_RATE_PREFIX.getName())) {
            String[] keys = StringUtils.split(configKey, "\\.");
            if (keys.length == 4) {
                String topic = keys[2];
                String app = keys[3];
                if (topic != null && app != null) {
                    cleanRateLimiter(topic, app, Subscription.Type.CONSUMPTION);
                }
            }
        }
    }
}
