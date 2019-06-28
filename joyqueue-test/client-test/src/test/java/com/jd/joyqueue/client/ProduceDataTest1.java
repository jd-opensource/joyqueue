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
package com.jd.joyqueue.client;

import io.openmessaging.KeyValue;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;
import org.junit.Assert;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/13
 */
public class ProduceDataTest1 {

    private String[] topics;
    private Producer producer;

    public ProduceDataTest1(String[] topics) {
        this.topics = topics;

        KeyValue attributes = OMS.newKeyValue();
        attributes.put(OMSBuiltinKeys.ACCOUNT_KEY, AbstractClientTest.ACCOUNT_KEY);
        producer = OMS.getMessagingAccessPoint(String.format("oms:joyqueue://%s@%s/%s",
                AbstractClientTest.ACCOUNT_ID, AbstractClientTest.SERVER, AbstractClientTest.REGION), attributes).createProducer();
        producer.start();
    }

    public void testProduce() {
        for (String topic : topics) {
            for (int i = 0; i < 100; i++) {
                Message message = producer.createMessage(topic, "test_body".getBytes());
                SendResult sendResult = producer.send(message);
                Assert.assertEquals("-1", sendResult.messageId());
            }
        }
    }
}