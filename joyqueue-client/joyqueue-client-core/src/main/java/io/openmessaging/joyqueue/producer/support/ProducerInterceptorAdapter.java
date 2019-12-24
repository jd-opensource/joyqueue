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
package io.openmessaging.joyqueue.producer.support;

import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendResult;
import org.joyqueue.client.internal.producer.interceptor.ProduceContext;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;
import io.openmessaging.interceptor.Context;
import io.openmessaging.joyqueue.producer.interceptor.ContextAdapter;
import io.openmessaging.joyqueue.producer.message.OMSProduceMessage;

import java.util.List;

/**
 * ProducerInterceptorAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class ProducerInterceptorAdapter implements ProducerInterceptor {

    private static final String OMS_CONTEXT_ATTRIBUTE_KEY = "_OMS_CONTEXT_";

    private io.openmessaging.interceptor.ProducerInterceptor omsProducerInterceptor;

    public ProducerInterceptorAdapter(io.openmessaging.interceptor.ProducerInterceptor omsProducerInterceptor) {
        this.omsProducerInterceptor = omsProducerInterceptor;
    }

    @Override
    public boolean preSend(ProduceContext context) {
        Context omsContext = getOrCreateOMSContext(context);
        for (ProduceMessage produceMessage : context.getMessages()) {
            omsProducerInterceptor.preSend(((OMSProduceMessage) produceMessage).getOmsMessage(), omsContext);
        }
        return true;
    }

    @Override
    public void postSend(ProduceContext context, List<SendResult> result) {
        Context omsContext = getOrCreateOMSContext(context);
        for (ProduceMessage produceMessage : context.getMessages()) {
            omsProducerInterceptor.postSend(((OMSProduceMessage) produceMessage).getOmsMessage(), omsContext);
        }
    }

    protected Context getOrCreateOMSContext(ProduceContext context) {
        Context omsContext = context.getAttribute(OMS_CONTEXT_ATTRIBUTE_KEY);
        if (omsContext == null) {
            omsContext = new ContextAdapter(context);
            context.putAttribute(OMS_CONTEXT_ATTRIBUTE_KEY, omsContext);
        }
        return omsContext;
    }

    @Override
    public int hashCode() {
        return omsProducerInterceptor.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProducerInterceptorAdapter)) {
            return false;
        }
        return omsProducerInterceptor.equals(((ProducerInterceptorAdapter) obj).getOmsProducerInterceptor());
    }

    @Override
    public String toString() {
        return omsProducerInterceptor.toString();
    }

    public io.openmessaging.interceptor.ProducerInterceptor getOmsProducerInterceptor() {
        return omsProducerInterceptor;
    }
}