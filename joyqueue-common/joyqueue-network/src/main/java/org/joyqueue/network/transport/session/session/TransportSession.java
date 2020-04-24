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
package org.joyqueue.network.transport.session.session;

import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.network.transport.session.session.config.TransportSessionConfig;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TransportSession
 *
 * author: gaohaoxiang
 * date: 2019/4/12
 */
public class TransportSession extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransportSession.class);

    private int id;
    private String host;
    private int port;
    private SocketAddress address;
    private ClientConfig clientConfig;
    private TransportSessionConfig config;
    private TransportClient transportClient;

    private volatile Transport transport;
    private volatile AtomicLong lastReconnect = new AtomicLong();

    public TransportSession(int id, String host, int port, ClientConfig clientConfig, TransportSessionConfig config, TransportClient transportClient) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.address = new InetSocketAddress(host, port);
        this.clientConfig = clientConfig;
        this.config = config;
        this.transportClient = transportClient;
        this.transport = initTransport();
    }

    protected Transport initTransport() {
        try {
            return transportClient.createTransport(address);
        } catch (Exception e) {
            logger.error("create transport session exception, address: {}", address, e);
            return null;
        }
    }

    @Override
    protected void doStop() {
        transport.stop();
    }

    public void async(Command request, int timeout, CommandCallback callback) {
        if (!checkTransport()) {
            callback.onException(request, new TransportException.ConnectionException(address.toString()));
            return;
        }
        transport.async(request, timeout, callback);
    }

    public Command sync(Command request, int timeout) {
        if (!checkTransport()) {
            throw new TransportException.ConnectionException(address.toString());
        }
        return transport.sync(request, timeout);
    }

    protected boolean checkTransport() {
        if (transport != null) {
            return true;
        }
        long lastReconnect = this.lastReconnect.get();
        if (SystemClock.now() - lastReconnect < config.getReconnectInterval()) {
            return false;
        }
        if (!this.lastReconnect.compareAndSet(lastReconnect, SystemClock.now())) {
            return false;
        }
        transport = initTransport();
        return (transport != null);
    }

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}