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
package com.jd.joyqueue.network.event;

import com.jd.joyqueue.network.transport.RequestBarrier;
import com.jd.joyqueue.network.transport.TransportHelper;
import com.jd.joyqueue.toolkit.concurrent.EventBus;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * TransportEventHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/15
 */
@ChannelHandler.Sharable
public class TransportEventHandler extends ChannelInboundHandlerAdapter {

    private RequestBarrier requestBarrier;
    private EventBus<TransportEvent> eventBus;

    public TransportEventHandler(RequestBarrier requestBarrier, EventBus<TransportEvent> eventBus) {
        this.requestBarrier = requestBarrier;
        this.eventBus = eventBus;
        try {
            this.eventBus.start();
        } catch (Exception e) {
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        eventBus.add(new TransportEvent(TransportEventType.CONNECT, TransportHelper.getOrNewTransport(ctx.channel(), requestBarrier)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        eventBus.add(new TransportEvent(TransportEventType.CLOSE, TransportHelper.getOrNewTransport(ctx.channel(), requestBarrier)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        eventBus.add(new TransportEvent(TransportEventType.EXCEPTION, TransportHelper.getOrNewTransport(ctx.channel(), requestBarrier)));
    }
}