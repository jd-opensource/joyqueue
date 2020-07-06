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
package org.joyqueue.client.samples.api.producer;

import org.joyqueue.client.internal.common.ordered.Ordered;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendResult;
import org.joyqueue.client.internal.producer.interceptor.ProduceContext;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;

import java.util.List;

/**
 * JoyQueueSimpleProducerInterceptor
 *
 * author: gaohaoxiang
 * date: 2019/4/8
 */
// Ordered接口提供getOrder方法，用于指定顺序，可以不实现
// context还有attributes等可使用，具体看org.joyqueue.client.internal.producer.interceptor.ProduceContext
public class JoyQueueSimpleProducerInterceptor implements ProducerInterceptor, Ordered {

    @Override
    public boolean preSend(ProduceContext context) {
        System.out.println("preSend");

        // 循环一批消息，单条生产和批量生产都是按批拦截
        for (ProduceMessage message : context.getMessages()) {
        }

        // 返回true表示这批消息可以生产，返回false表示这批消息不可生产
        return true;
    }

    @Override
    public void postSend(ProduceContext context, List<SendResult> result) {
        System.out.println("postSend");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}