package com.jd.journalq.client;

import io.openmessaging.KeyValue;
import io.openmessaging.OMS;
import io.openmessaging.jmq.consumer.ExtensionConsumer;
import io.openmessaging.jmq.domain.JMQConsumerBuiltinKeys;
import org.junit.Before;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/12
 */
public class AbstractConsumerTest extends AbstractClientTest {

    public ExtensionConsumer consumer;

    @Override
    @Before
    public void before() {
        super.before();

        consumer = (ExtensionConsumer) messagingAccessPoint.createConsumer();
        consumer.start();
    }

    @Override
    protected KeyValue getAttributes() {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(JMQConsumerBuiltinKeys.LONGPOLL_TIMEOUT, -1);
        keyValue.put(JMQConsumerBuiltinKeys.BROADCAST_LOCAL_PATH, "/export/Data/jmq/broadcast");
        return keyValue;
    }
}