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
package org.joyqueue.broker.network.protocol.support;

import io.netty.channel.ChannelHandler;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.network.protocol.ChannelHandlerProvider;
import org.joyqueue.network.protocol.CommandHandlerProvider;
import org.joyqueue.network.protocol.ExceptionHandlerProvider;
import org.joyqueue.network.protocol.ProtocolServer;
import org.joyqueue.network.transport.codec.CodecFactory;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.joyqueue.network.transport.config.ServerConfig;
import org.joyqueue.toolkit.lang.LifeCycle;

import java.util.concurrent.ExecutorService;

/**
 * ProtocolServerWrapper
 * author: gaohaoxiang
 * date: 2019/12/26
 */
public class ProtocolServerWrapper implements LifeCycle, ProtocolServer, BrokerContextAware, ExceptionHandlerProvider, CommandHandlerProvider, ChannelHandlerProvider {

    private ProtocolServer delegate;
    private ExecutorService commonThreadPool;
    private ExecutorService fetchThreadPool;
    private ExecutorService produceThreadPool;

    public ProtocolServerWrapper(ProtocolServer delegate, ExecutorService commonThreadPool, ExecutorService fetchThreadPool, ExecutorService produceThreadPool) {
        this.delegate = delegate;
        this.commonThreadPool = commonThreadPool;
        this.fetchThreadPool = fetchThreadPool;
        this.produceThreadPool = produceThreadPool;
    }

    @Override
    public void start() throws Exception {
        if (delegate instanceof LifeCycle) {
            ((LifeCycle) delegate).start();
        }
    }

    @Override
    public void stop() {
        if (delegate instanceof LifeCycle) {
            ((LifeCycle) delegate).stop();
        }
    }

    @Override
    public boolean isStarted() {
        if (delegate instanceof LifeCycle) {
            return ((LifeCycle) delegate).isStarted();
        }
        return false;
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        if (delegate instanceof BrokerContextAware) {
            ((BrokerContextAware) delegate).setBrokerContext(brokerContext);
        }
    }

    @Override
    public ChannelHandler getChannelHandler(ChannelHandler channelHandler) {
        if (delegate instanceof ChannelHandlerProvider) {
            return ((ChannelHandlerProvider) delegate).getChannelHandler(channelHandler);
        }
        return null;
    }

    @Override
    public ChannelHandler getCommandHandler(ChannelHandler channelHandler) {
        if (delegate instanceof CommandHandlerProvider) {
            return ((CommandHandlerProvider) delegate).getCommandHandler(channelHandler);
        }
        return null;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        if (delegate instanceof ExceptionHandlerProvider) {
            return ((ExceptionHandlerProvider) delegate).getExceptionHandler();
        }
        return null;
    }

    @Override
    public ServerConfig createServerConfig(ServerConfig serverConfig) {
        return delegate.createServerConfig(serverConfig);
    }

    @Override
    public CodecFactory createCodecFactory() {
        return delegate.createCodecFactory();
    }

    @Override
    public CommandHandlerFactory createCommandHandlerFactory() {
        return new CommandHandlerFactoryWrapper(delegate.createCommandHandlerFactory(), commonThreadPool, fetchThreadPool, produceThreadPool);
    }

    @Override
    public String type() {
        return delegate.type();
    }
}