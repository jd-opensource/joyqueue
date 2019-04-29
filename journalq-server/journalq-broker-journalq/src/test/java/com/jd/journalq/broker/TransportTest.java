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
package com.jd.journalq.broker;

/**
 * TransportTest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
// FIXME: 测试未通过
public class TransportTest {

//    @Test
//    public void test() throws Exception {
//        TransportClient transportClient = new BrokerTransportClientFactory().create(new ClientConfig());
//        Transport transport = transportClient.createTransport(new InetSocketAddress(IpUtil.getLocalIp(), 50088));
//
//        JMQHeader header = new JMQHeader(Direction.REQUEST, CommandType.CONSUME_INDEX_QUERY_REQUEST);
//        ConsumeIndexQueryRequest request = new ConsumeIndexQueryRequest("test", Maps.newHashMap());
//
//        Command response = transport.sync(new Command(header, request));
//        System.out.println(JSON.toJSONString(response));
//    }
}