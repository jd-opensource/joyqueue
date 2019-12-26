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
package org.joyqueue.broker.network.protocol;

import org.joyqueue.network.protocol.ProtocolService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * ProtocolResolver
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public class ProtocolResolver extends ByteToMessageDecoder {

    protected static final Logger logger = LoggerFactory.getLogger(ProtocolResolver.class);

    private ProtocolManager protocolManager;
    private Map<String /** protocol **/, ProtocolContext> protocolContextMapper;

    public ProtocolResolver(ProtocolManager protocolManager, Map<String, ProtocolContext> protocolContextMapper) {
        this.protocolManager = protocolManager;
        this.protocolContextMapper = protocolContextMapper;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        ProtocolContext protocolContext = matchProtocol(ctx, buffer);

        if (protocolContext == null) {
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            logger.error("unsupported protocol, ctx: {}, buffer: {}", ctx, ArrayUtils.toString(bytes));
            ctx.close();
            return;
        }

        bindProtocol(ctx, protocolContext);

        // 执行其他处理类
        ctx.pipeline().fireChannelRegistered()
                .fireChannelActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    }

    /**
     * 绑定协议
     * @param ctx
     * @param protocolContext
     */
    protected void bindProtocol(ChannelHandlerContext ctx, ProtocolContext protocolContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("bind protocol, protocol: {}, ctx: {}", protocolContext.getProtocol().type(), ctx);
        }

        // 绑定协议handler
        ctx.pipeline()
                .addLast(protocolContext.getHandlerPipeline())
                .remove(this);
    }

    /**
     * 匹配协议
     * @param buffer
     * @return
     */
    protected ProtocolContext matchProtocol(ChannelHandlerContext ctx, ByteBuf buffer) {
        List<ProtocolService> protocols = protocolManager.getProtocolServices();
        int readerIndex = buffer.readerIndex();
        for (ProtocolService protocol : protocols) {
            try {
                // 是否支持
                if (protocol.isSupport(buffer)) {
                    return getProtocolContext(protocol);
                }
            } catch (Exception e) {
                logger.error("resolve protocol exception, ctx: {}, protocol: {}", ctx, protocol, e);
            } finally {
                buffer.readerIndex(readerIndex);
            }
        }
        return null;
    }

    protected ProtocolContext getProtocolContext(ProtocolService protocolService) {
        return protocolContextMapper.get(protocolService.type());
    }
}