package com.jd.journalq.client;

import io.openmessaging.KeyValue;
import io.openmessaging.OMS;
import io.openmessaging.journalq.consumer.ExtensionConsumer;
import io.openmessaging.journalq.domain.JournalQConsumerBuiltinKeys;
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
        keyValue.put(JournalQConsumerBuiltinKeys.LONGPOLL_TIMEOUT, -1);
        keyValue.put(JournalQConsumerBuiltinKeys.BROADCAST_LOCAL_PATH, "/export/Data/jmq/broadcast");
        return keyValue;
    }
}