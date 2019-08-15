package io.openmessaging.joyqueue.producer.support;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendResult;
import io.chubao.joyqueue.client.internal.producer.interceptor.ProduceContext;
import io.chubao.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;
import io.openmessaging.interceptor.Context;
import io.openmessaging.joyqueue.producer.interceptor.ContextAdapter;
import io.openmessaging.joyqueue.producer.message.OMSProduceMessage;

import java.util.List;

/**
 * ProducerInterceptorAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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