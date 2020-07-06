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
package org.joyqueue.client.samples.api.interceptor;

import org.joyqueue.client.internal.common.ordered.Ordered;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendResult;
import org.joyqueue.client.internal.producer.interceptor.ProduceContext;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;

import java.util.List;

/**
 * SimpleMessageFilterProducerInterceptor
 *
 * author: gaohaoxiang
 * date: 2019/4/8
 */
// 简单消息过滤实现
public class SimpleMessageFilterProducerInterceptor implements ProducerInterceptor, Ordered {

    @Override
    public boolean preSend(ProduceContext context) {
        for (ProduceMessage message : context.getMessages()) {
            message.putAttribute(SimpleMessageFilterConsts.ENVIRONMENT_ATTR_KEY, SimpleMessageFilterConsts.CURRENT_ENVIRONMENT);
        }
        return true;
    }

    @Override
    public void postSend(ProduceContext context, List<SendResult> result) {

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}