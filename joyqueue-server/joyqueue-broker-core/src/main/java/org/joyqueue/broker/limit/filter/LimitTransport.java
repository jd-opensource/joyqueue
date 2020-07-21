package org.joyqueue.broker.limit.filter;

import org.joyqueue.broker.network.traffic.RequestTrafficPayload;
import org.joyqueue.broker.network.traffic.ResponseTrafficPayload;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.TransportAttribute;
import org.joyqueue.network.transport.TransportState;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.exception.TransportException;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * LimitTransport
 * author: gaohaoxiang
 * date: 2020/7/15
 */
public class LimitTransport implements Transport {

    private Transport delegate;
    private boolean isRequired;
    private AbstractLimitFilter abstractLimitFilter;
    private RequestTrafficPayload requestTrafficPayload;

    public LimitTransport(Transport delegate, boolean isRequired, AbstractLimitFilter abstractLimitFilter, RequestTrafficPayload requestTrafficPayload) {
        this.delegate = delegate;
        this.isRequired = isRequired;
        this.abstractLimitFilter = abstractLimitFilter;
        this.requestTrafficPayload = requestTrafficPayload;
    }

    @Override
    public Command sync(Command command) throws TransportException {
        return delegate.sync(command);
    }

    @Override
    public Command sync(Command command, long timeout) throws TransportException {
        return delegate.sync(command, timeout);
    }

    @Override
    public void async(Command command, CommandCallback callback) throws TransportException {
        delegate.async(command, callback);
    }

    @Override
    public void async(Command command, long timeout, CommandCallback callback) throws TransportException {
        delegate.async(command, timeout, callback);
    }

    @Override
    public CompletableFuture<?> async(Command command) throws TransportException {
        return delegate.async(command);
    }

    @Override
    public CompletableFuture<?> async(Command command, long timeout) throws TransportException {
        return delegate.async(command, timeout);
    }

    @Override
    public void oneway(Command command) throws TransportException {
        delegate.oneway(command);
    }

    @Override
    public void oneway(Command command, long timeout) throws TransportException {
        delegate.oneway(command, timeout);
    }

    @Override
    public void acknowledge(Command request, Command response) throws TransportException {
        try {
            ResponseTrafficPayload responseTrafficPayload = abstractLimitFilter.getResponseTrafficPayload(request, response);
            if (responseTrafficPayload == null) {
                delegate.acknowledge(request, response);
                return;
            }

            if (requestTrafficPayload != null && requestTrafficPayload.getTraffic().isLimited()) {
                response = abstractLimitFilter.doLimit(delegate, request, response, isRequired);
            } else if (abstractLimitFilter.limitIfNeeded(responseTrafficPayload)) {
                response = abstractLimitFilter.doLimit(delegate, request, response, isRequired);
            }
            if (response != null) {
                delegate.acknowledge(request, response);
            }
        } finally {
            if (isRequired && requestTrafficPayload != null) {
                abstractLimitFilter.releaseRequire(requestTrafficPayload);
            }
        }
    }

    @Override
    public void acknowledge(Command request, Command response, CommandCallback callback) throws TransportException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocketAddress remoteAddress() {
        return delegate.remoteAddress();
    }

    @Override
    public TransportAttribute attr() {
        return delegate.attr();
    }

    @Override
    public void attr(TransportAttribute attribute) {
        delegate.attr(attribute);
    }

    @Override
    public TransportState state() {
        return delegate.state();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}