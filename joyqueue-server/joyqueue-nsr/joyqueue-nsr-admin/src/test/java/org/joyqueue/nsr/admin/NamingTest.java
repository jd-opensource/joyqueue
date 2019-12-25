/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.nsr.admin;

import org.joyqueue.domain.Broker;
import org.joyqueue.nsr.admin.AdminClient;
import org.joyqueue.nsr.admin.BrokerAdmin;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class NamingTest {

    private static final String ConnectionStr="196.0.0.1:50091,127.0.0.1:50091";
    @Test
    public void multiNodeTest() throws Exception{
        AdminClient client=new AdminClient(ConnectionStr);
        BrokerAdmin.ListArg listArg=new BrokerAdmin.ListArg();
        int maxTries=3;
        List<Broker> brokers=null;
        while(maxTries-->0) {
            try {
                brokers= client.listBroker(listArg);
            } catch (Exception e) {

            }
        }
        Assert.assertNotEquals(null,brokers);
    }
}
