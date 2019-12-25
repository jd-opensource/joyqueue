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
package io.openmessaging.joyqueue.consumer.support;

import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.interceptor.ConsumeContext;
import org.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;
import io.openmessaging.interceptor.Context;
import io.openmessaging.joyqueue.consumer.interceptor.ContextAdapter;
import io.openmessaging.joyqueue.consumer.message.MessageConverter;
import io.openmessaging.message.Message;

import java.util.List;

/**
 * ConsumerInterceptorAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/20
 */
public class ConsumerInterceptorAdapter implements ConsumerInterceptor {

    private static final String OMS_CONTEXT_ATTRIBUTE_KEY = "_OMS_CONTEXT_";

    private static final String OMS_MESSAGES_ATTRIBUTE_KEY = "_OMS_MESSAGES_";

    private io.openmessaging.interceptor.ConsumerInterceptor omsConsumerInterceptor;

    public ConsumerInterceptorAdapter(io.openmessaging.interceptor.ConsumerInterceptor omsConsumerInterceptor) {
        this.omsConsumerInterceptor = omsConsumerInterceptor;
    }

    @Override
    public boolean preConsume(ConsumeContext context) {
        Context omsContext = getOrCreateOMSContext(context);
        List<Message> omsMessages = getOrConvertMessages(context);
        for (Message omsMessage : omsMessages) {
            omsConsumerInterceptor.preReceive(omsMessage, omsContext);
        }
        return true;
    }

    @Override
    public void postConsume(ConsumeContext context, List<ConsumeReply> consumeReplies) {
        Context omsContext = getOrCreateOMSContext(context);
        List<Message> omsMessages = getOrConvertMessages(context);
        for (Message omsMessage : omsMessages) {
            omsConsumerInterceptor.postReceive(omsMessage, omsContext);
        }
    }

    protected List<Message> getOrConvertMessages(ConsumeContext context) {
        List<Message> omsMessages = context.getAttribute(OMS_MESSAGES_ATTRIBUTE_KEY);
        if (omsMessages == null) {
            omsMessages = MessageConverter.convertMessages(context.getMessages());
            context.putAttribute(OMS_MESSAGES_ATTRIBUTE_KEY, omsMessages);
        }
        return omsMessages;
    }

    protected Context getOrCreateOMSContext(ConsumeContext context) {
        Context omsContext = context.getAttribute(OMS_CONTEXT_ATTRIBUTE_KEY);
        if (omsContext == null) {
            omsContext = new ContextAdapter(context);
            context.putAttribute(OMS_CONTEXT_ATTRIBUTE_KEY, omsContext);
        }
        return omsContext;
    }

    @Override
    public int hashCode() {
        return omsConsumerInterceptor.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConsumerInterceptorAdapter)) {
            return false;
        }
        return omsConsumerInterceptor.equals(((ConsumerInterceptorAdapter) obj).getOmsConsumerInterceptor());
    }

    @Override
    public String toString() {
        return omsConsumerInterceptor.toString();
    }

    public io.openmessaging.interceptor.ConsumerInterceptor getOmsConsumerInterceptor() {
        return omsConsumerInterceptor;
    }
}