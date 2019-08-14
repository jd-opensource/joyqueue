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
package io.chubao.joyqueue.broker.kafka.session;

import io.chubao.joyqueue.network.transport.ChannelTransport;
import io.chubao.joyqueue.network.transport.TransportAttribute;
import io.chubao.joyqueue.network.transport.TransportState;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandCallback;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

    public boolean tryAcquire(int timeout, TimeUnit timeUnit) {
        try {
            return semaphore.tryAcquire(timeout, timeUnit);
        } catch (InterruptedException e) {
            logger.error("wait acquire exception", e);
            return false;
        }
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

    @Override
    public String toString() {
        return delegate.toString();
    }
}