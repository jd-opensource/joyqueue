package io.chubao.joyqueue.client.internal.consumer.transport;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.client.internal.transport.Client;
import io.chubao.joyqueue.client.internal.transport.ClientGroup;

import java.util.List;

/**
 * ConsumerClientGroup
 *
 * author: gaohaoxiang
 * date: 2019/2/12
 */
public class ConsumerClientGroup {

    private ClientGroup clientGroup;

    public ConsumerClientGroup(ClientGroup clientGroup) {
        this.clientGroup = clientGroup;
    }

    public List<ConsumerClient> getClients() {
        List<Client> clients = clientGroup.getClients();
        List<ConsumerClient> result = Lists.newArrayListWithCapacity(clients.size());
        for (Client client : clients) {
            result.add(ConsumerClient.build(client));
        }
        return result;
    }
}