package io.openmessaging.joyqueue.producer.interceptor;

import io.chubao.joyqueue.client.internal.producer.interceptor.ProduceContext;
import io.openmessaging.KeyValue;
import io.openmessaging.interceptor.Context;

/**
 * ContextAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class ContextAdapter implements Context {

    private ProduceContext context;

    private KeyValue attributes;

    public ContextAdapter(ProduceContext consumeContext) {
        this.context = consumeContext;
    }

    @Override
    public KeyValue attributes() {
        if (attributes == null) {
            attributes = new ContextAttributeAdapter(context);
        }
        return attributes;
    }
}