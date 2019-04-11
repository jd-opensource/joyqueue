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
package com.jd.journalq.broker.kafka.handler.ratelimit;

import com.google.common.collect.Sets;
import com.jd.journalq.broker.kafka.command.KafkaRequestOrResponse;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.handler.AbstractKafkaCommandHandler;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.broker.kafka.util.RateLimiter;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.toolkit.delay.DelayedOperationKey;
import com.jd.journalq.toolkit.delay.DelayedOperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaRateLimitHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/12
 */
public class KafkaRateLimitHandler extends AbstractKafkaCommandHandler {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaRateLimitHandler.class);

    private KafkaConfig config;
    private AbstractKafkaCommandHandler delegate;
    private DelayedOperationManager delayedOperationManager;
    private RateLimiter rateLimiter;

    public KafkaRateLimitHandler(KafkaConfig config, AbstractKafkaCommandHandler delegate, DelayedOperationManager delayedOperationManager, RateLimiter rateLimiter) {
        this.config = config;
        this.delegate = delegate;
        this.delayedOperationManager = delayedOperationManager;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        Object payload = command.getPayload();
        KafkaHeader header = (KafkaHeader) command.getHeader();

        if (!config.isRateLimitEnable() || !config.isRateLimit(header.getType()) || !(payload instanceof KafkaRequestOrResponse)) {
            return delegate.handle(transport, command);
        }

        KafkaRequestOrResponse request = (KafkaRequestOrResponse) payload;
        String key = generateDelayKey(request, transport);

        if (rateLimiter.tryAcquire(key, header.getType())) {
            return delegate.handle(transport, command);
        }

        int rateLimitDelay = config.getRateLimitDelay(header.getType());
        logger.info("request rate is limited, type: {}, key: {}, delay: {}", request.type(), key, rateLimitDelay);

        Command response = delegate.handle(transport, command);
        delayedOperationManager.tryCompleteElseWatch(
                new RateLimitDelayedOperation(rateLimitDelay, delegate, transport, command, response),
                Sets.newHashSet(new DelayedOperationKey(key)));
        return null;
    }

    protected String generateDelayKey(KafkaRequestOrResponse request, Transport transport) {
        return request.type() + "_" + transport.remoteAddress().toString();
    }

    @Override
    public int type() {
        return delegate.type();
    }
}