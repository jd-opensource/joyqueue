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
        keyValue.put(JournalQConsumerBuiltinKeys.BROADCAST_LOCAL_PATH, "/export/Data/journalq/broadcast");
        return keyValue;
    }
}
