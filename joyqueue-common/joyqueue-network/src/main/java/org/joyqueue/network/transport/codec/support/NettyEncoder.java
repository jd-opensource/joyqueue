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
package org.joyqueue.network.transport.codec.support;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.joyqueue.network.transport.codec.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NettyEncoder
 *
 * author: gaohaoxiang
 * date: 2018/8/14
 */
public class NettyEncoder extends MessageToByteEncoder {

    protected static final Logger logger = LoggerFactory.getLogger(NettyEncoder.class);

    private Codec codec;

    public NettyEncoder(Codec codec) {
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        try {
            codec.encode(msg, out);
        } catch (Exception e) {
            logger.error("encode exception, ctx: {}, msg: {}", ctx, msg, e);
            ctx.channel().close();
        }
    }
}