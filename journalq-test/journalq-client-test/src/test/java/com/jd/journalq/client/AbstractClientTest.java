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

import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/12
 */
public class AbstractClientTest {

    public static final String SERVER = IpUtil.getLocalIp() + ":50088";

    public static final String ACCOUNT_ID = "test_app";

    public static final String ACCOUNT_KEY = "test_token";

    public static final String REGION = "UNKNOWN";

    protected MessagingAccessPoint messagingAccessPoint;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void before() {
        KeyValue attributes = getAttributes();
        attributes.put(OMSBuiltinKeys.ACCOUNT_KEY, ACCOUNT_KEY);
        messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:journalq://%s@%s/%s", ACCOUNT_ID, SERVER, REGION), attributes);
    }

    protected KeyValue getAttributes() {
        return OMS.newKeyValue();
    }
}