package com.jd.journalq.common.network.transport.support;

import com.jd.journalq.common.network.event.TransportEvent;
import com.jd.journalq.common.network.event.TransportEventType;
import com.jd.journalq.common.network.transport.ChannelTransport;
import com.jd.journalq.common.network.transport.TransportAttribute;
import com.jd.journalq.common.network.transport.TransportClient;
import com.jd.journalq.common.network.transport.TransportState;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.CommandCallback;
import com.jd.journalq.common.network.transport.config.TransportConfig;
import com.jd.journalq.common.network.transport.exception.TransportException;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.retry.RetryPolicy;
import com.jd.journalq.toolkit.time.SystemClock;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.Future;

/**
 * 故障切换通信
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/3
 */
public class FailoverChannelTransport implements ChannelTransport {

    protected static final Logger logger = LoggerFactory.getLogger(FailoverChannelTransport.class);

    private volatile ChannelTransport delegate;
    private SocketAddress address;
    private long connectionTimeout;
    private TransportClient transportClient;
    private TransportConfig config;
    private EventBus<TransportEvent> transportEventBus;
    private volatile long lastReconnect;

    public FailoverChannelTransport(ChannelTransport delegate, SocketAddress address, long connectionTimeout, TransportClient transportClient, TransportConfig config, EventBus<TransportEvent> transportEventBus) {
        this.delegate = delegate;
        this.address = address;
        this.connectionTimeout = connectionTimeout;
        this.transportClient = transportClient;
        this.config = config;
        this.transportEventBus = transportEventBus;
    }

    @Override
    public Channel getChannel() {
        return delegate.getChannel();
    }

    @Override
    public Command sync(Command command) throws TransportException {
        return sync(command, 0);
    }

    @Override
    public Command sync(Command command, long timeout) throws TransportException {
        RetryPolicy retryPolicy = config.getRetryPolicy();
        TransportException lastException = null;
        Command response = null;
        int retryTimes = 0;

        for (int i = 0, retryLimit = retryPolicy.getMaxRetrys(); i <= retryLimit; i++) {
            try {
                response = delegate.sync(command, timeout);
                break;
            } catch (TransportException e) {
                if (!(e instanceof TransportException.RequestTimeoutException)) {
                    if (!tryReconnect()) {
                        // 重连失败，抛出异常
                        throw e;
                    }
                }

                lastException = e;
                retryTimes++;
            }
        }

        // 如果有过异常，并且没有重试成功，抛出异常
        if (lastException != null && response == null) {
            throw lastException;
        }

        // 有过重试，打印日志
        if (lastException != null) {
            logger.warn("transport sync exception, retry {} times success, command: {}, timeout: {}", retryTimes, command, timeout, lastException);
        }

        return response;
    }

    @Override
    public void async(Command command, CommandCallback callback) throws TransportException {
        async(command, 0, callback);
    }

    @Override
    public void async(final Command command, final long timeout, final CommandCallback callback) throws TransportException {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        if (!checkChannel()) {
            callback.onException(command, TransportException.RequestErrorException.build(IpUtil.toAddress(delegate.getChannel().remoteAddress())));
            return;
        }
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
        oneway(command, 0);
    }

    @Override
    public void oneway(Command command, long timeout) throws TransportException {
        delegate.oneway(command, timeout);
    }

    @Override
    public void acknowledge(Command request, Command response) throws TransportException {
        delegate.acknowledge(request, response);
    }

    @Override
    public void acknowledge(Command request, Command response, CommandCallback callback) throws TransportException {
        delegate.acknowledge(request, response, callback);
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

    protected boolean checkChannel() {
        if (isChannelActive()) {
            return true;
        }
        return tryReconnect();
    }

    protected boolean tryReconnect() {
        if (!isNeedReconnect()) {
            return false;
        }
        synchronized (this) {
            // 判断重连间隔
            if (isNeedReconnect()) {
                return reconnect();
            } else {
                return false;
            }
        }
    }

    protected boolean isChannelActive() {
        return delegate.getChannel().isActive();
    }

    protected boolean isNeedReconnect() {
        return SystemClock.now() - lastReconnect > config.getRetryPolicy().getMaxRetryDelay();
    }

    protected boolean reconnect() {
        try {
            ChannelTransport newTransport = (ChannelTransport) transportClient.createTransport(address, connectionTimeout);
            ChannelTransport delegate = this.delegate;
            this.delegate = newTransport;
            try {
                delegate.stop();
            } catch (Throwable t) {
                logger.warn("stop transport exception, transport: {}", newTransport, t);
            }
            logger.info("reconnect transport success, transport: {}", newTransport);
            transportEventBus.add(new TransportEvent(TransportEventType.RECONNECT, newTransport));
            return true;
        } catch (Throwable t) {
            logger.debug("reconnect transport exception, address: {}", address, t);
//            logger.warn("reconnect transport exception, address: {}", address);
            return false;
        } finally {
            lastReconnect = SystemClock.now();
        }
    }
}