/**
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
package com.jd.journalq.network.transport.support;

import com.jd.journalq.network.event.TransportEvent;
import com.jd.journalq.network.transport.ChannelTransport;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.TransportClient;
import com.jd.journalq.network.transport.TransportClientSupport;
import com.jd.journalq.network.transport.config.TransportConfig;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;

import java.net.SocketAddress;

/**
 * FailoverTransportClient
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/30
 */
public class FailoverTransportClient implements TransportClient {

    private TransportClient delegate;
    private TransportConfig config;
    private EventBus<TransportEvent> transportEventBus;

    public FailoverTransportClient(TransportClient delegate, TransportConfig config, EventBus<TransportEvent> transportEventBus) {
        this.delegate = delegate;
        this.config = config;
        this.transportEventBus = transportEventBus;
    }

    @Override
    public Transport createTransport(String address) throws TransportException {
        return this.createTransport(address, -1);
    }

    @Override
    public Transport createTransport(String address, long connectionTimeout) throws TransportException {
        return this.createTransport(TransportClientSupport.createInetSocketAddress(address), connectionTimeout);
    }

    @Override
    public Transport createTransport(SocketAddress address) throws TransportException {
        return this.createTransport(address, -1);
    }

    @Override
    public Transport createTransport(SocketAddress address, long connectionTimeout) throws TransportException {
        ChannelTransport transport = (ChannelTransport) delegate.createTransport(address, connectionTimeout);
        return new FailoverChannelTransport(transport, address, connectionTimeout, delegate, config, transportEventBus);
    }

    @Override
    public void addListener(EventListener<TransportEvent> listener) {
        delegate.addListener(listener);
    }

    @Override
    public void removeListener(EventListener<TransportEvent> listener) {
        delegate.removeListener(listener);
    }

    @Override
    public void start() throws Exception {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public boolean isStarted() {
        return delegate.isStarted();
    }
}