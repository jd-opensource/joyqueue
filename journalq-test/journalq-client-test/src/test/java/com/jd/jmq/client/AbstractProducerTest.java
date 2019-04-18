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

import io.openmessaging.KeyValue;
import io.openmessaging.journalq.domain.JMQProducerBuiltinKeys;
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
        attributes.put(JMQProducerBuiltinKeys.COMPRESS_THRESHOLD, 1);
        return attributes;
    }
}