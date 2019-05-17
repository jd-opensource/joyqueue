/**
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
package com.jd.journalq.broker.limit.filter;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.Plugins;
import com.jd.journalq.broker.helper.AwareHelper;
import com.jd.journalq.broker.limit.LimitRejectedStrategy;
import com.jd.journalq.broker.limit.RateLimitManager;
import com.jd.journalq.broker.limit.RateLimiter;
import com.jd.journalq.broker.limit.config.LimitConfig;
import com.jd.journalq.broker.limit.domain.LimitContext;
import com.jd.journalq.broker.limit.support.DefaultRateLimiterManager;
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
    private RateLimitManager rateLimiterManager;
    private LimitRejectedStrategy limitRejectedStrategy;

    @Override
    protected boolean isEnable() {
        return config.isEnable();
    }

    @Override
    protected boolean limitIfNeeded(String topic, String app, String type, Traffic traffic) {
        RateLimiter rateLimiter = rateLimiterManager.getRateLimiter(topic, app, type);
        if (rateLimiter == null) {
            return false;
        }
        return !rateLimiter.tryAcquireTps() || !rateLimiter.tryAcquireTraffic(traffic.getTraffic(topic));
    }

    @Override
    protected Command doLimit(Transport transport, Command request, Command response) {
        int delay = getDelay(transport, request, response);
        LimitContext limitContext = new LimitContext(transport, request, response, delay);

        if (logger.isDebugEnabled()) {
            logger.debug("traffic limit, transport: {}, request: {}, response: {}, delay: {}", transport, request, response, delay);
        }
        return limitRejectedStrategy.execute(limitContext);
    }

    protected int getDelay(Transport transport, Command request, Command response) {
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