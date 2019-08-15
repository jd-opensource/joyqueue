package io.chubao.joyqueue.network.transport.codec;

import io.chubao.joyqueue.network.transport.command.Header;
import io.netty.buffer.ByteBuf;

/**
 * PayloadDecoder
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public interface PayloadDecoder<H extends Header> {

    Object decode(H header, ByteBuf buffer) throws Exception;
}