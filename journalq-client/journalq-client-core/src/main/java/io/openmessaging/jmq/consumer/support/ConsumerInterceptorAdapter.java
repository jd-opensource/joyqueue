package io.openmessaging.jmq.consumer.support;

import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumeContext;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumerInterceptor;
import io.openmessaging.interceptor.Context;
import io.openmessaging.jmq.consumer.interceptor.ContextAdapter;
import io.openmessaging.jmq.consumer.message.MessageConverter;
import io.openmessaging.message.Message;

import java.util.List;

/**
 * ConsumerInterceptorAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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