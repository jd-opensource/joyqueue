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

import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author LiYue
 * Date: 2020/3/5
 */
public class ReplicationTransportSession {

    protected static final Logger logger = LoggerFactory.getLogger(ReplicationTransportSession.class);

    // TODO 参数化
    private static final long RECONNECT_INTERVAL = 1000 * 60;

    private TransportClient transportClient;
    private Transport transport;
    private String address;
    private volatile long lastReconnect;

    public ReplicationTransportSession(String address, TransportClient transportClient) {
        this.address = address;
        this.transportClient = transportClient;
        this.transport = initTransport();
    }

    public void start() throws TransportException {
        try {
            transportClient.start();
        } catch (TransportException te) {
            throw te;
        } catch (Exception e ) {
            throw new TransportException.UnknownException("", e);
        }
    }

    public void sendCommand(Command request, int timeout, CommandCallback callback) {
        if (transport == null) {
            if (SystemClock.now() - lastReconnect < RECONNECT_INTERVAL) {
                callback.onException(request, new TransportException.ConnectionException(address));
                return;
            }
            transport = initTransport();
            if (transport == null) {
                callback.onException(request, new TransportException.ConnectionException(address));
                return;
            }
        }
        transport.async(request, timeout, callback);
    }

    public void stop() {
        if (transport != null) {
            transport.stop();
        }
    }

    protected Transport initTransport() {
        try {
            return transportClient.createTransport(address);
        } catch (Exception e) {
            logger.error("create transport session exception, address: {}", address, e);
            return null;
        } finally {
            lastReconnect = SystemClock.now();
        }
    }
}
