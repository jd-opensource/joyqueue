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
import io.netty.handler.codec.ByteToMessageDecoder;
import org.joyqueue.network.transport.codec.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * NettyDecoder
 *
 * author: gaohaoxiang
 * date: 2018/8/14
 */
public class NettyDecoder extends ByteToMessageDecoder {

    protected static final Logger logger = LoggerFactory.getLogger(NettyDecoder.class);

    private Codec codec;

    public NettyDecoder(Codec codec) {
        this.codec = codec;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            Object payload = codec.decode(in);
            if (payload != null) {
                out.add(payload);
            }
        } catch (Exception e) {
            logger.error("decode exception, ctx: {}", ctx, e);
            ctx.channel().close();
        }
    }
}