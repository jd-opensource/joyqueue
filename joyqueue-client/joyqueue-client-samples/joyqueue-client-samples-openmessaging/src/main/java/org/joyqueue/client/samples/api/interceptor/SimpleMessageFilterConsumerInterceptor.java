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
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.interceptor.ConsumeContext;
import org.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * SimpleMessageFilterConsumerInterceptor
 *
 * author: gaohaoxiang
 * date: 2019/4/8
 */
// 简单消息过滤实现
public class SimpleMessageFilterConsumerInterceptor implements ConsumerInterceptor, Ordered {

    @Override
    public boolean preConsume(ConsumeContext context) {
        for (ConsumeMessage message : context.getMessages()) {
            String environment = message.getAttribute(SimpleMessageFilterConsts.ENVIRONMENT_ATTR_KEY);
            if (!StringUtils.equals(environment, SimpleMessageFilterConsts.CURRENT_ENVIRONMENT)) {
                context.filterMessage(message);
            }
        }
        return true;
    }

    @Override
    public void postConsume(ConsumeContext context, List<ConsumeReply> consumeReplies) {

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}