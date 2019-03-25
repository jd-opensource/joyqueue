package com.jd.journalq.client;

import io.openmessaging.KeyValue;
import io.openmessaging.journalq.domain.JMQConsumerBuiltinKeys;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/12
 */
public class ConsumerTest2 extends ConsumerTest1 {

    @Override
    protected KeyValue getAttributes() {
        KeyValue keyValue = super.getAttributes();
        keyValue.put(JMQConsumerBuiltinKeys.LOADBALANCE, false);
        return keyValue;
    }
}