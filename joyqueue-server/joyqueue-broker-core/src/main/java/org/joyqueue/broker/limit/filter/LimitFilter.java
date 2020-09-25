/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.limit.filter;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.Plugins;
import org.joyqueue.broker.helper.AwareHelper;
import org.joyqueue.broker.limit.LimitRejectedStrategy;
import org.joyqueue.broker.limit.RateLimitManager;
import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.broker.limit.config.LimitConfig;
import org.joyqueue.broker.limit.domain.LimitContext;
import org.joyqueue.broker.limit.support.DefaultRateLimiterManager;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.handler.filter.CommandHandlerInvocation;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LimitFilter
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class LimitFilter extends AbstractLimitFilter implements BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(LimitFilter.class);

    private LimitConfig config;
    private RateLimitManager rateLimiterManager;
    private LimitRejectedStrategy limitRejectedStrategy;

    @Override
    public Command invoke(CommandHandlerInvocation invocation) throws TransportException {
        if (!config.isEnable()) {
            return invocation.invoke();
        }
        return super.invoke(invocation);
    }

    @Override
    protected boolean requireIfAcquired(String topic, String app, String type) {
        RateLimiter rateLimiter = rateLimiterManager.getRateLimiter(topic, app, type);
        if (rateLimiter == null) {
            return false;
        }
        return rateLimiter.tryAcquireRequire();
    }

    @Override
    protected boolean releaseRequire(String topic, String app, String type) {
        RateLimiter rateLimiter = rateLimiterManager.getRateLimiter(topic, app, type);
        if (rateLimiter == null) {
            return false;
        }
        return rateLimiter.releaseRequire();
    }

    @Override
    protected boolean limitIfNeeded(String topic, String app, String type, Traffic traffic) {
        RateLimiter rateLimiter = rateLimiterManager.getRateLimiter(topic, app, type);
        if (rateLimiter == null) {
            return false;
        }
        return !rateLimiter.tryAcquireTps(traffic.getTps(topic)) || !rateLimiter.tryAcquireTraffic(traffic.getTraffic(topic));
    }

    @Override
    protected Command doLimit(Transport transport, Command request, Command response, boolean isRequired) {
        int delay = getDelay(transport, request, response, isRequired);
        LimitContext limitContext = new LimitContext(transport, request, response, delay);

        if (logger.isDebugEnabled()) {
            logger.debug("traffic limit, transport: {}, request: {}, response: {}, delay: {}", transport, request, response, delay);
        }
        return limitRejectedStrategy.execute(limitContext);
    }

    protected int getDelay(Transport transport, Command request, Command response, boolean isRequired) {
        if (!isRequired) {
            return config.getConflictDelay();
        }
        int delay = config.getDelay();
        if (delay == LimitConfig.DELAY_DYNAMIC) {
            int dynamicDelay = (int) (1000 - (SystemClock.now() % 1000));
            delay = Math.min(dynamicDelay, config.getMaxDelay());
        }
        return delay;
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.config = new LimitConfig(brokerContext.getPropertySupplier());
        this.rateLimiterManager = new DefaultRateLimiterManager(brokerContext);
        this.limitRejectedStrategy = AwareHelper.enrichIfNecessary(Plugins.LIMIT_REJECTED_STRATEGY.get(config.getRejectedStrategy()), brokerContext);
    }
}