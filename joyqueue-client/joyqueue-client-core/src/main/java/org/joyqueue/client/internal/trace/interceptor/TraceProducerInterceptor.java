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
package org.joyqueue.client.internal.trace.interceptor;

import org.joyqueue.client.internal.common.ordered.Ordered;
import org.joyqueue.client.internal.producer.domain.SendResult;
import org.joyqueue.client.internal.producer.interceptor.ProduceContext;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;
import org.joyqueue.client.internal.trace.TraceBuilder;
import org.joyqueue.client.internal.trace.TraceCaller;
import org.joyqueue.client.internal.trace.TraceType;

import java.util.List;

/**
 * TraceProducerInterceptor
 *
 * author: gaohaoxiang
 * date: 2019/1/11
 */
public class TraceProducerInterceptor implements ProducerInterceptor, Ordered {

    private static final String CALLER_KEY = "_TRACE_CALLER_";

    @Override
    public boolean preSend(ProduceContext context) {
        TraceCaller caller = TraceBuilder.newInstance()
                .topic(context.getTopic())
                .app(context.getApp())
                .namespace(context.getNameserver().getNamespace())
                .type(TraceType.PRODUCER_SEND)
                .begin();
        context.putAttribute(CALLER_KEY, caller);
        return true;
    }

    @Override
    public void postSend(ProduceContext context, List<SendResult> result) {
        TraceCaller caller = context.getAttribute(CALLER_KEY);
        if (caller == null) {
            return;
        }
        caller.end();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}