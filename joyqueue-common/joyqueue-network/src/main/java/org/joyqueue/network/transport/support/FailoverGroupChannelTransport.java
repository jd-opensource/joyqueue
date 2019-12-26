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
package org.joyqueue.network.transport.support;

import org.joyqueue.network.event.TransportEvent;
import org.joyqueue.network.transport.ChannelTransport;
import org.joyqueue.network.transport.TransportAttribute;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.TransportState;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.config.TransportConfig;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.time.SystemClock;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * FailoverGroupChannelTransport
 *
 * author: gaohaoxiang
 * date: 2019/7/22
 */
public class FailoverGroupChannelTransport implements ChannelTransport {

    protected static final Logger logger = LoggerFactory.getLogger(FailoverGroupChannelTransport.class);

    private List<SocketAddress> addresses;
    private long connectionTimeout;
    private TransportClient transportClient;
    private TransportConfig config;
    private EventBus<TransportEvent> transportEventBus;

    private volatile int roundrobinIndex = 0;
    private ConcurrentMap<SocketAddress, ChannelTransportEntry> transports = new ConcurrentHashMap<>();

    public FailoverGroupChannelTransport(List<SocketAddress> addresses, long connectionTimeout,
                                         TransportClient transportClient, TransportConfig config, EventBus<TransportEvent> transportEventBus) {
        this.addresses = addresses;
        this.connectionTimeout = connectionTimeout;
        this.transportClient = transportClient;
        this.config = config;
        this.transportEventBus = transportEventBus;
        init();
    }

    @Override
    public Command sync(Command command) throws TransportException {
        return sync(command, 0);
    }

    @Override
    public void async(Command command, CommandCallback callback) throws TransportException {
        this.async(command, 0, callback);
    }

    @Override
    public CompletableFuture<?> async(Command command) throws TransportException {
        return this.async(command, 0);
    }

    @Override
    public void oneway(Command command) throws TransportException {
        this.oneway(command, 0);
    }

    @Override
    public void acknowledge(Command request, Command response) throws TransportException {
        this.acknowledge(request, response, null);
    }

    @Override
    public Command sync(Command command, long timeout) throws TransportException {
        return execute((transport) -> {
            return transport.sync(command, timeout);
        });
    }

    @Override
    public void async(Command command, long timeout, CommandCallback callback) throws TransportException {
        execute((transport) -> {
            transport.async(command, timeout, callback);
            return null;
        });
    }

    @Override
    public CompletableFuture<?> async(Command command, long timeout) throws TransportException {
        return execute((transport) -> {
            return transport.async(command, timeout);
        });
    }

    @Override
    public void oneway(Command command, long timeout) throws TransportException {
        execute((transport) -> {
            transport.oneway(command, timeout);
            return null;
        });
    }

    @Override
    public void acknowledge(Command request, Command response, CommandCallback callback) throws TransportException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocketAddress remoteAddress() {
        return execute((transport) -> {
            return transport.remoteAddress();
        });
    }

    @Override
    public TransportAttribute attr() {
        return execute((transport) -> {
            return transport.attr();
        });
    }

    @Override
    public void attr(TransportAttribute attribute) {
        execute((transport) -> {
            transport.attr(attribute);
            return null;
        });
    }

    @Override
    public TransportState state() {
        for (Map.Entry<SocketAddress, ChannelTransportEntry> entry : transports.entrySet()) {
            ChannelTransport transport = entry.getValue().getTransport();
            if (transport != null && transport.state().equals(TransportState.CONNECTED)) {
                return TransportState.CONNECTED;
            }
        }
        return TransportState.DISCONNECTED;
    }

    @Override
    public void stop() {
        for (Map.Entry<SocketAddress, ChannelTransportEntry> entry : transports.entrySet()) {
            ChannelTransport transport = entry.getValue().getTransport();
            if (transport != null) {
                transport.stop();
            }
        }
    }

    @Override
    public Channel getChannel() {
        return execute((transport) -> {
            return transport.getChannel();
        });
    }

    protected void init() {
        for (SocketAddress address : addresses) {
            try {
                getOrCreateTransport(address);
                return;
            } catch (TransportException e) {
                logger.warn("create transport exception, address: {}", address, e);
                roundrobinIndex++;
            }
        }
        throw new TransportException.ConnectionException();
    }

    protected <T> T execute(Function<ChannelTransport, T> function) throws TransportException {
        int addressesSize = addresses.size();
        int index = roundrobinIndex;

        for (int i = 0; i < addressesSize; i++) {
            if (index >= addressesSize) {
                index = 0;
            }

            SocketAddress address = addresses.get(index);

            try {
                ChannelTransport transport = getOrCreateTransport(address);
                T result = function.apply(transport);
                roundrobinIndex = index;
                return result;
            } catch (TransportException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("execute exception, address: {}", address, e);
                }
                index++;
            }
        }
        throw new TransportException.RequestErrorException();
    }

    protected ChannelTransport getOrCreateTransport(SocketAddress address) throws TransportException {
        ChannelTransportEntry transportEntry = transports.get(address);
        long now = SystemClock.now();

        if (transportEntry != null) {
            if (transportEntry.getTransport() != null) {
                return transportEntry.getTransport();
            }
            if (now - transportEntry.getLastConnect() < config.getRetryDelay()) {
                throw new TransportException.ConnectionException(address.toString());
            }
        }

        synchronized (transports) {
            transportEntry = transports.get(address);

            if (transportEntry != null) {
                if (transportEntry.getTransport() != null) {
                    return transportEntry.getTransport();
                }
                if (now - transportEntry.getLastConnect() < config.getRetryDelay()) {
                    throw new TransportException.ConnectionException(address.toString());
                }
            } else {
                transportEntry = new ChannelTransportEntry();
            }

            ChannelTransport transport = null;
            try {
                transport = createTransport(address);
                transportEntry.setTransport(transport);

                if (logger.isDebugEnabled()) {
                    logger.debug("create transport, address: {}", address);
                }

                return transport;
            } catch (TransportException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("create transport exception, address: {}", address, e);
                }
                throw e;
            } finally {
                transportEntry.setLastConnect(now);
                transports.put(address, transportEntry);
            }
        }
    }

    protected ChannelTransport createTransport(SocketAddress address) throws TransportException {
        ChannelTransport transport = (ChannelTransport) transportClient.createTransport(address, connectionTimeout);
        return new FailoverChannelTransport(transport, address, connectionTimeout, transportClient, config, transportEventBus);
    }

    class ChannelTransportEntry {
        private ChannelTransport transport;
        private volatile long lastConnect;

        ChannelTransportEntry() {

        }

        ChannelTransportEntry(long lastConnect) {
            this.lastConnect = lastConnect;
        }

        ChannelTransportEntry(ChannelTransport transport, long lastConnect) {
            this.transport = transport;
            this.lastConnect = lastConnect;
        }

        public ChannelTransport getTransport() {
            return transport;
        }

        public void setTransport(ChannelTransport transport) {
            this.transport = transport;
        }

        public long getLastConnect() {
            return lastConnect;
        }

        public void setLastConnect(long lastConnect) {
            this.lastConnect = lastConnect;
        }
    }
}