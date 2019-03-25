package com.jd.journalq.client;

import com.jd.journalq.common.exception.JMQCode;
import io.openmessaging.KeyValue;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.jmq.domain.JMQProducerBuiltinKeys;
import io.openmessaging.jmq.producer.ExtensionProducer;
import io.openmessaging.message.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/12
 */
public class ProducerTest2 extends AbstractProducerTest {

    public static final String TOPIC = "test_produce_nearby_1";

    @Before
    public void before() {
        KeyValue attributes = getAttributes();
        attributes.put(OMSBuiltinKeys.ACCOUNT_KEY, ACCOUNT_KEY);
        attributes.put(JMQProducerBuiltinKeys.TRANSACTION_TIMEOUT, 1000 * 10);
        messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:jmq://%s@%s/%s", ACCOUNT_ID, SERVER, "test_region"), attributes);

        producer = (ExtensionProducer) messagingAccessPoint.createProducer();
        producer.start();
    }

    @Test
    public void testSend() {
        Message message = producer.createMessage(TOPIC, "test_body".getBytes());
        Assert.assertEquals(message.header().getDestination(), TOPIC);
        Assert.assertEquals(new String(message.getData()), "test_body");

        try {
            producer.send(message);
        } catch (OMSRuntimeException e) {
            Assert.assertEquals(e.getErrorCode(), JMQCode.FW_TOPIC_NO_PARTITIONGROUP.getCode());
        }
    }
}