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
package com.jd.joyqueue.network.transport.support;

import com.jd.joyqueue.network.event.TransportEvent;
import com.jd.joyqueue.network.event.TransportEventHandler;
import com.jd.joyqueue.network.handler.ExceptionChannelHandler;
import com.jd.joyqueue.network.transport.RequestBarrier;
import com.jd.joyqueue.network.transport.TransportServerSupport;
import com.jd.joyqueue.network.transport.codec.Codec;
import com.jd.joyqueue.network.transport.codec.support.NettyDecoder;
import com.jd.joyqueue.network.transport.codec.support.NettyEncoder;
import com.jd.joyqueue.network.transport.command.CommandDispatcher;
import com.jd.joyqueue.network.transport.command.handler.ExceptionHandler;
import com.jd.joyqueue.network.transport.command.support.DefaultCommandDispatcher;
import com.jd.joyqueue.network.transport.command.support.RequestHandler;
import com.jd.joyqueue.network.transport.command.support.ResponseHandler;
import com.jd.joyqueue.network.transport.config.ServerConfig;
import com.jd.joyqueue.network.transport.handler.CommandInvocation;
import com.jd.joyqueue.toolkit.concurrent.EventBus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;

/**
 * DefaultTransportServer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/22
 */
public class DefaultTransportServer extends TransportServerSupport {

    private Codec codec;
    private ExceptionHandler exceptionHandler;
    private RequestBarrier requestBarrier;
    private RequestHandler requestHandler;
    private ResponseHandler responseHandler;
    private EventBus<TransportEvent> transportEventBus;

    public DefaultTransportServer(ServerConfig serverConfig, String host, int port, Codec codec,
                                  ExceptionHandler exceptionHandler, RequestBarrier requestBarrier, RequestHandler requestHandler,
                                  ResponseHandler responseHandler, EventBus<TransportEvent> transportEventBus) {
        super(serverConfig, host, port);
        this.codec = codec;
        this.exceptionHandler = exceptionHandler;
        this.requestBarrier = requestBarrier;
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
        this.transportEventBus = transportEventBus;
    }

    @Override
    protected ChannelHandler newChannelHandlerPipeline() {
        final CommandDispatcher commandDispatcher = new DefaultCommandDispatcher(requestBarrier, requestHandler, responseHandler);
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline()
                        .addLast(new NettyDecoder(codec))
                        .addLast(new NettyEncoder(codec))
                        .addLast(new TransportEventHandler(requestBarrier, transportEventBus))
                        .addLast(new ExceptionChannelHandler(exceptionHandler, requestBarrier))
                        .addLast(new CommandInvocation(commandDispatcher));
            }
        };
    }
}