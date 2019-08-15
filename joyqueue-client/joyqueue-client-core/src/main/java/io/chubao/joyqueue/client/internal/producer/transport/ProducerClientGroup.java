package io.chubao.joyqueue.client.internal.producer.transport;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.client.internal.transport.Client;
import io.chubao.joyqueue.client.internal.transport.ClientGroup;

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