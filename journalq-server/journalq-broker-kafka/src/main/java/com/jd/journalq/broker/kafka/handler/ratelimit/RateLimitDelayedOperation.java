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

import com.jd.journalq.broker.kafka.KafkaCommandHandler;
import com.jd.journalq.broker.kafka.command.KafkaRequestOrResponse;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.toolkit.delay.AbstractDelayedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RateLimitDelayedOperation
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/12
 */
public class RateLimitDelayedOperation extends AbstractDelayedOperation {

    protected static final Logger logger = LoggerFactory.getLogger(RateLimitDelayedOperation.class);

    private int dealyMs;
    private KafkaCommandHandler delegate;
    private Transport transport;
    private Command request;
    private Command response;

    public RateLimitDelayedOperation(int dealyMs, KafkaCommandHandler delegate, Transport transport, Command request, Command response) {
        super(dealyMs);
        this.dealyMs = dealyMs;
        this.delegate = delegate;
        this.transport = transport;
        this.request = request;
        this.response = response;
    }

    @Override
    protected void onComplete() {
        try {
            if (response != null) {
                if (response.getPayload() instanceof KafkaRequestOrResponse) {
                    ((KafkaRequestOrResponse) response.getPayload()).setThrottleTimeMs(dealyMs);
                }
                transport.acknowledge(request, response);
            }
        } catch (Exception e) {
            logger.error("send response failed", e);
        }
    }
}