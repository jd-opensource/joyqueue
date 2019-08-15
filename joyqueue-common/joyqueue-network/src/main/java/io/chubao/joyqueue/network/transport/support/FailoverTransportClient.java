package io.chubao.joyqueue.network.transport.support;

import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.ChannelTransport;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.TransportClient;
import io.chubao.joyqueue.network.transport.TransportClientSupport;
import io.chubao.joyqueue.network.transport.config.TransportConfig;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * FailoverTransportClient
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/30
 */
public class FailoverTransportClient implements TransportClient {

    private static final String GROUP_SPLITTER = ",";

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
        String[] addresses = StringUtils.splitByWholeSeparator(address, GROUP_SPLITTER);
        if (addresses.length == 1) {
            return this.createTransport(TransportClientSupport.createInetSocketAddress(address), connectionTimeout);
        } else {
            List<SocketAddress> socketAddresses = new ArrayList<>(addresses.length);
            for (String addressItem : addresses) {
                InetSocketAddress socketAddress = TransportClientSupport.createInetSocketAddress(addressItem);
                socketAddresses.add(socketAddress);
            }
            return createGroupTransport(socketAddresses, connectionTimeout);
        }
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

    protected Transport createGroupTransport(List<SocketAddress> addresses, long connectionTimeout) throws TransportException {
        return new FailoverGroupChannelTransport(addresses, connectionTimeout, delegate, config, transportEventBus);
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