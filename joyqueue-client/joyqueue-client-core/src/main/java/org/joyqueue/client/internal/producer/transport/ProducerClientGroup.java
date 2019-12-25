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
package org.joyqueue.client.internal.producer.transport;

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.transport.Client;
import org.joyqueue.client.internal.transport.ClientGroup;

import java.util.List;

/**
 * ProducerClientGroup
 *
 * author: gaohaoxiang
 * date: 2019/2/12
 */
public class ProducerClientGroup {

    private ClientGroup clientGroup;

    public ProducerClientGroup(ClientGroup clientGroup) {
        this.clientGroup = clientGroup;
    }

    public List<ProducerClient> getClients() {
        List<Client> clients = clientGroup.getClients();
        List<ProducerClient> result = Lists.newArrayListWithCapacity(clients.size());
        for (Client client : clients) {
            result.add(ProducerClient.build(client));
        }
        return result;
    }
}