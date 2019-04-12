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

import com.jd.journalq.broker.kafka.handler.AbstractKafkaCommandHandler;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.util.RateLimiter;
import com.jd.journalq.toolkit.delay.DelayedOperationManager;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaRateLimitHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/12
 */
public class KafkaRateLimitHandlerFactory extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaRateLimitHandlerFactory.class);

    private KafkaConfig config;
    private DelayedOperationManager delayedOperationManager;
    private RateLimiter rateLimiter;

    public KafkaRateLimitHandlerFactory(KafkaConfig config, DelayedOperationManager delayedOperationManager, RateLimiter rateLimiter) {
        this.config = config;
        this.delayedOperationManager = delayedOperationManager;
        this.rateLimiter = rateLimiter;
    }

    public AbstractKafkaCommandHandler create(AbstractKafkaCommandHandler delegate) {
        return new KafkaRateLimitHandler(config, delegate, delayedOperationManager, rateLimiter);
    }

    @Override
    protected void doStart() throws Exception {
        delayedOperationManager.start();
    }

    @Override
    protected void doStop() {
        delayedOperationManager.shutdown();
    }
}