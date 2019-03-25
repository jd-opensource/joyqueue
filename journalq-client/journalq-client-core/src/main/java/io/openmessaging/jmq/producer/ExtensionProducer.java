package io.openmessaging.jmq.producer;

import io.openmessaging.producer.Producer;

/**
 * ExtensionProducer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/4
 */
public interface ExtensionProducer extends Producer {

    ExtensionTransactionalResult prepare();

    ExtensionTransactionalResult prepare(String transactionId);
}