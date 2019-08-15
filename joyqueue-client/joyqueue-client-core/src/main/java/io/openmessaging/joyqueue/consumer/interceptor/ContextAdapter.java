package io.openmessaging.joyqueue.consumer.interceptor;

import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumeContext;
import io.openmessaging.KeyValue;
import io.openmessaging.interceptor.Context;

/**
 * ContextAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class ContextAdapter implements Context {

    private ConsumeContext context;

    private KeyValue attributes;

    public ContextAdapter(ConsumeContext context) {
        this.context = context;
    }

    @Override
    public KeyValue attributes() {
        if (attributes == null) {
            attributes = new ContextAttributeAdapter(context);
        }
        return attributes;
    }
}