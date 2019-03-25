package com.jd.journalq.common.network.transport.codec.support;

import com.jd.journalq.common.network.transport.codec.Codec;
import com.jd.journalq.common.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * netty编码器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public class NettyEncoder extends MessageToByteEncoder {

    private Codec codec;

    public NettyEncoder(Codec codec) {
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        try {
            codec.encode(msg, out);
        } catch (Exception e) {
            if (e instanceof TransportException.CodecException) {
                throw e;
            } else {
                throw new TransportException.CodecException(e.getMessage());
            }
        }
    }
}