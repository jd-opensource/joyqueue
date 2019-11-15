package io.chubao.joyqueue.client.internal.producer.transport;

import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;

/**
 * ClientConnectionListener
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class ProducerClientConnectionListener implements EventListener<TransportEvent> {

    private Transport transport;
    private ProducerClient client;

    public ProducerClientConnectionListener(Transport transport, ProducerClient client) {
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
                client.addProducers();
            }
        }
    }
}