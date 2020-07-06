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
package org.joyqueue.client.samples.api.consumer;

import org.joyqueue.client.internal.common.ordered.Ordered;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.interceptor.ConsumeContext;
import org.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;

import java.util.List;

/**
 * JoyQueueSimpleConsumerInterceptor
 *
 * author: gaohaoxiang
 * date: 2019/4/8
 */
// Ordered接口提供getOrder方法，用于指定顺序，可以不实现
// context还有attributes等可使用，具体看org.joyqueue.client.internal.consumer.interceptor.ConsumeContext
public class JoyQueueSimpleConsumerInterceptor implements ConsumerInterceptor, Ordered {

    @Override
    public boolean preConsume(ConsumeContext context) {
        System.out.println("preConsume");

        // 循环一批消息，单条和批消息都是按批拦截
        for (ConsumeMessage message : context.getMessages()) {
            // 过滤消息
            context.filterMessage(message);
        }

        // 返回true表示这批消息可以消费，返回false表示这批消息不可消费
        return true;
    }

    @Override
    public void postConsume(ConsumeContext context, List<ConsumeReply> consumeReplies) {
        System.out.println("postConsume");
    }

    @Override
    public int getOrder() {
        // 值小的先执行
        return Ordered.LOWEST_PRECEDENCE;
    }
}