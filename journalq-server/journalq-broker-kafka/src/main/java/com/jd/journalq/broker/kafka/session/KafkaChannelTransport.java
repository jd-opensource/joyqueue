package com.jd.journalq.broker.kafka.session;

import com.jd.journalq.network.transport.ChannelTransport;
import com.jd.journalq.network.transport.TransportAttribute;
import com.jd.journalq.network.transport.TransportState;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.exception.TransportException;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * KafkaChannelTransport
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/7
 */
public class KafkaChannelTransport implements ChannelTransport {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaChannelTransport.class);

    private ChannelTransport delegate;
    private Semaphore semaphore = new Semaphore(1, true);

    public KafkaChannelTransport(ChannelTransport delegate) {
        this.delegate = delegate;
    }

    @Override
    public Channel getChannel() {
        return delegate.getChannel();
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
    public Future<?> async(Command command) throws TransportException {
        return delegate.async(command);
    }

    @Override
    public Future<?> async(Command command, long timeout) throws TransportException {
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
        acknowledge(request, response, null);
    }

    @Override
    public void acknowledge(Command request, Command response, CommandCallback callback) throws TransportException {
        delegate.getChannel().eventLoop().execute(() -> {
            delegate.acknowledge(request, response, callback);
        });
        release();
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

    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    public void acquire() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            logger.error("wait acquire exception", e);
        }
    }

    public void release() {
        semaphore.release();
    }
}