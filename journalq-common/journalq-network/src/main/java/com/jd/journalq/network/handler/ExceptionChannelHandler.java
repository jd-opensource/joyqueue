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
package com.jd.journalq.network.handler;

import com.jd.journalq.network.transport.RequestBarrier;
import com.jd.journalq.network.transport.TransportHelper;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * ExceptionChannelHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/5
 */
public class ExceptionChannelHandler extends ChannelHandlerAdapter {

    private ExceptionHandler exceptionHandler;
    private RequestBarrier requestBarrier;

    public ExceptionChannelHandler(ExceptionHandler exceptionHandler, RequestBarrier requestBarrier) {
        this.exceptionHandler = exceptionHandler;
        this.requestBarrier = requestBarrier;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (exceptionHandler != null) {
            exceptionHandler.handle(TransportHelper.getOrNewTransport(ctx.channel(), requestBarrier), null, cause);
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}