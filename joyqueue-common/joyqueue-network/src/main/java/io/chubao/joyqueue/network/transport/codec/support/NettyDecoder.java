package io.chubao.joyqueue.network.transport.codec.support;

import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * NettyDecoder
 *
 * author: gaohaoxiang
 * date: 2018/8/14
 */
public class NettyDecoder extends ByteToMessageDecoder {

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
            if (e instanceof TransportException.CodecException) {
                throw e;
            } else {
                throw new TransportException.CodecException(e);
            }
        }
    }
}