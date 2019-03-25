package com.jd.journalq.broker.network.protocol;

import com.jd.journalq.common.network.protocol.ProtocolService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 协议决定器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
        buffer.retain();
        ctx.pipeline().fireChannelRegistered()
                .fireChannelActive().fireChannelRead(buffer);
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
                .remove(this)
                .addLast(protocolContext.getHandlerPipeline());
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