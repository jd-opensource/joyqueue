package io.chubao.joyqueue.client.internal.transport;

import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;

/**
 * ClientConnectionListener
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class ClientConnectionListener implements EventListener<TransportEvent> {

    private Transport transport;
    private Client client;

    public ClientConnectionListener(Transport transport, Client client) {
        this.transport = transport;
        this.client = client;
    }

    @Override
    public void onEvent(TransportEvent event) {
        if (event.getTransport() != transport) {
            return;
        }
        switch (event.getType()) {
            case RECONNECT: {
                client.handleAddConnection();
            }
        }
    }
}