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
package org.joyqueue.broker.kafka.session;

import org.joyqueue.network.transport.ChannelTransport;
import org.joyqueue.network.transport.TransportAttribute;
import org.joyqueue.network.transport.TransportState;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.exception.TransportException;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * KafkaChannelTransport
 *
 * author: gaohaoxiang
 * date: 2019/5/7
 */
public class KafkaChannelTransport implements ChannelTransport {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaChannelTransport.class);

    private ChannelTransport delegate;
    private ConcurrentLinkedQueue<Command> requestQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentMap<Command, Command> responseMap = new ConcurrentHashMap<>();

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
    public synchronized void acknowledge(Command request, Command response) throws TransportException {
        responseMap.put(request, response);
        while ((request = requestQueue.peek()) != null) {
            Command queueRequest = request;
            Command queueResponse = responseMap.get(queueRequest);
            if (queueResponse == null) {
                break;
            }

            delegate.getChannel().eventLoop().execute(() -> {
                delegate.acknowledge(queueRequest, queueResponse);
            });
            responseMap.remove(queueRequest);
            requestQueue.remove(queueRequest);
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

    public void acquire(Command request) {
        requestQueue.offer(request);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}