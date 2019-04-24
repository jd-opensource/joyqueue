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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.index.command.ConsumeIndexQueryRequest;
import com.jd.journalq.broker.network.support.BrokerTransportClientFactory;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.TransportClient;
import com.jd.journalq.network.transport.codec.JournalqHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.config.ClientConfig;
import com.jd.journalq.toolkit.network.IpUtil;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * TransportTest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class TransportTest {

    @Test
    public void test() throws Exception {
        TransportClient transportClient = new BrokerTransportClientFactory().create(new ClientConfig());
        Transport transport = transportClient.createTransport(new InetSocketAddress(IpUtil.getLocalIp(), 50088));

        JournalqHeader header = new JournalqHeader(Direction.REQUEST, CommandType.CONSUME_INDEX_QUERY_REQUEST);
        ConsumeIndexQueryRequest request = new ConsumeIndexQueryRequest("test", Maps.newHashMap());

        Command response = transport.sync(new Command(header, request));
        System.out.println(JSON.toJSONString(response));
    }
}