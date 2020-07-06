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

import org.joyqueue.client.internal.transport.config.TransportConfig;
import org.joyqueue.network.transport.TransportState;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClientHeartbeatThread
 *
 * author: gaohaoxiang
 * date: 2019/1/7
 */
public class ClientHeartbeatThread implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(ClientHeartbeatThread.class);

    private TransportConfig transportConfig;
    private ClientGroupManager clientGroupManager;

    public ClientHeartbeatThread(TransportConfig transportConfig, ClientGroupManager clientGroupManager) {
        this.transportConfig = transportConfig;
        this.clientGroupManager = clientGroupManager;
    }

    @Override
    public void run() {
        for (ClientGroup clientGroup : clientGroupManager.getGroups()) {
            doHeartbeat(clientGroup);
        }
    }

    protected void doHeartbeat(ClientGroup clientGroup) {
        for (Client client : clientGroup.getClients()) {
            if (client.getTransport().state().equals(TransportState.DISCONNECTED)
                    || (client.getLastUseTime() != 0 && SystemClock.now() - client.getLastUseTime() >= transportConfig.getHeartbeatMaxIdleTime())) {
                doHeartbeat(client);
            }
        }
    }

    protected void doHeartbeat(Client client) {
        try {
            client.heartbeat(transportConfig.getHeartbeatTimeout());
        } catch (Exception e) {
            logger.debug("client heartbeat exception", e);
        }
    }
}