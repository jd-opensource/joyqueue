/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.client;

import com.jd.journalq.exception.JournalqCode;
import io.openmessaging.KeyValue;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.journalq.domain.JournalQProducerBuiltinKeys;
import io.openmessaging.journalq.producer.ExtensionProducer;
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
        attributes.put(JournalQProducerBuiltinKeys.TRANSACTION_TIMEOUT, 1000 * 10);
        messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:journalq://%s@%s/%s", ACCOUNT_ID, SERVER, "test_region"), attributes);

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
            Assert.assertEquals(e.getErrorCode(), JournalqCode.FW_TOPIC_NO_PARTITIONGROUP.getCode());
        }
    }
}