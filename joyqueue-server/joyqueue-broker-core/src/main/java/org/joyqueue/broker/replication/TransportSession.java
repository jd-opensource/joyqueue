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
package org.joyqueue.broker.replication;

import org.joyqueue.broker.network.support.BrokerTransportClientFactory;
import org.joyqueue.network.event.TransportEvent;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.TransportAttribute;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.network.transport.support.DefaultTransportAttribute;
import org.joyqueue.toolkit.concurrent.EventListener;

/**
 * @author LiYue
 * Date: 2020/3/5
 */
public class TransportSession{
    private final Transport transport;
    private final TransportClient transportClient;
    private final EventListener<TransportEvent> eventEventListener;
    TransportSession(String address, ClientConfig clientConfig, EventListener<TransportEvent> eventEventListener) {
        transportClient = new BrokerTransportClientFactory().create(clientConfig);
        transport = transportClient.createTransport(address);
        this.eventEventListener = eventEventListener;
        TransportAttribute attribute = transport.attr();
        if (attribute == null) {
            attribute = new DefaultTransportAttribute();
            transport.attr(attribute);
        }
        attribute.set("address", address);
    }

    public void start() throws TransportException {
        try {
            transportClient.start();
            transportClient.addListener(eventEventListener);
        } catch (TransportException te) {
            throw te;
        } catch (Exception e ) {
            throw new TransportException.UnknownException("", e);
        }
    }

    public Transport getTransport() {
        return transport;
    }

    public void stop() {
        transport.stop();
        transportClient.stop();
    }
}
