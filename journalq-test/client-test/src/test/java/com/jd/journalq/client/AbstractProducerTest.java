package com.jd.journalq.client;

import io.openmessaging.KeyValue;
import io.openmessaging.journalq.domain.JournalQProducerBuiltinKeys;
import io.openmessaging.journalq.producer.ExtensionProducer;
import org.junit.Before;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/12
 */
public class AbstractProducerTest extends AbstractClientTest {

    public ExtensionProducer producer;

    @Override
    @Before
    public void before() {
        super.before();

        producer = (ExtensionProducer) messagingAccessPoint.createProducer();
        producer.start();
    }

    @Override
    protected KeyValue getAttributes() {
        KeyValue attributes = super.getAttributes();
        attributes.put(JournalQProducerBuiltinKeys.COMPRESS_THRESHOLD, 1);
        return attributes;
    }
}