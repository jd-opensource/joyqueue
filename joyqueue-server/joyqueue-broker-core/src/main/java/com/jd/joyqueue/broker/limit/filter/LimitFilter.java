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
package com.jd.joyqueue.broker.limit.filter;

import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.BrokerContextAware;
import com.jd.joyqueue.broker.Plugins;
import com.jd.joyqueue.broker.helper.AwareHelper;
import com.jd.joyqueue.broker.limit.LimitRejectedStrategy;
import com.jd.joyqueue.broker.limit.RateLimitManager;
import com.jd.joyqueue.broker.limit.RateLimiter;
import com.jd.joyqueue.broker.limit.config.LimitConfig;
import com.jd.joyqueue.broker.limit.domain.LimitContext;
import com.jd.joyqueue.broker.limit.support.DefaultRateLimiterManager;
import com.jd.joyqueue.broker.network.traffic.Traffic;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.handler.filter.CommandHandlerInvocation;
import com.jd.joyqueue.network.transport.exception.TransportException;
import com.jd.joyqueue.toolkit.time.SystemClock;
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
    public Command invoke(CommandHandlerInvocation invocation) throws TransportException {
        if (!config.isEnable()) {
            return invocation.invoke();
        }
        return super.invoke(invocation);
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