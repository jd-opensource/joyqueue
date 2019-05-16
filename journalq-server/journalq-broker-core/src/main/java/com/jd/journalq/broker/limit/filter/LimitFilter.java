package com.jd.journalq.broker.limit.filter;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.Plugins;
import com.jd.journalq.broker.helper.AwareHelper;
import com.jd.journalq.broker.limit.LimitRejectedStrategy;
import com.jd.journalq.broker.limit.RateLimiter;
import com.jd.journalq.broker.limit.RateLimiterManager;
import com.jd.journalq.broker.limit.config.LimitConfig;
import com.jd.journalq.broker.limit.domain.LimitContext;
import com.jd.journalq.broker.network.traffic.Traffic;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LimitFilter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class LimitFilter extends AbstractLimitFilter implements BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(LimitFilter.class);

    private LimitConfig config;
    private RateLimiterManager rateLimiterManager;
    private LimitRejectedStrategy limitRejectedStrategy;

    @Override
    protected boolean isEnable() {
        return config.isEnable();
    }

    @Override
    protected boolean limitIfNeeded(String topic, String app, String trafficType, Traffic traffic) {
        RateLimiter rateLimiter = rateLimiterManager.getRateLimiter(topic, app, trafficType);
        return !rateLimiter.tryAcquireTps() || !rateLimiter.tryAcquireTraffic(traffic.getTraffic(topic));
    }

    @Override
    protected Command doLimit(Transport transport, Command request, Command response) {
        int delay = getDelay(transport, request, response);
        LimitContext limitContext = new LimitContext(transport, request, response, delay);

        if (logger.isInfoEnabled()) {
            logger.info("traffic limit, transport: {}, request: {}, response: {}, delay: {}", transport, request, response, delay);
        }
        return limitRejectedStrategy.execute(limitContext);
    }

    protected int getDelay(Transport transport, Command request, Command response) {
        int delay = config.getDelay();
        if (delay == LimitConfig.DELAY_DYNAMIC) {
            long now = SystemClock.now();
            int dynamicDelay = (int) (1000 - (now % 1000));
            delay = Math.min(dynamicDelay, config.getMaxDelay());
        }
        return delay;
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.config = new LimitConfig(brokerContext.getPropertySupplier());
        this.rateLimiterManager = new RateLimiterManager(brokerContext);
        this.limitRejectedStrategy = AwareHelper.enrichIfNecessary(Plugins.LIMIT_REJECTED_STRATEGY.get(config.getRejectedStrategy()), brokerContext);
    }
}