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
package org.joyqueue.client.internal.transport;

import org.joyqueue.network.event.TransportEvent;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.toolkit.concurrent.EventListener;

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